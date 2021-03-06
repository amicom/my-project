//jDownloader - Downloadmanager
//Copyright (C) 2010  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.IOException;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDHexUtils;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision: 28560 $", interfaceVersion = 2, names = { "wetransfer.com" }, urls = { "https?://(www\\.)?((wtrns\\.fr|we\\.tl)/[\\w\\-]+|wetransfer\\.com/downloads/[a-z0-9]+/[a-z0-9]+(/[a-z0-9]+)?)" }, flags = { 0 })
public class WeTransferCom extends PluginForHost {

    private String HASH   = null;
    private String CODE   = null;
    private String DLLINK = null;

    public WeTransferCom(final PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://wetransfer.info/terms/";
    }

    private String getAMFRequest() {
        final String data = "0A000000020200" + getHexLength(CODE) + JDHexUtils.getHexString(CODE) + "0200" + getHexLength(HASH) + JDHexUtils.getHexString(HASH);
        return JDHexUtils.toString("000000000001002177657472616E736665722E446F776E6C6F61642E636865636B446F776E6C6F616400022F31000000" + getHexLength(JDHexUtils.toString(data)) + data);
    }

    private String getHexLength(final String s) {
        final String result = Integer.toHexString(s.length());
        return result.length() % 2 > 0 ? "0" + result : result;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        setBrowserExclusive();
        String dlink = link.getDownloadURL();
        if (dlink.matches("http://(wtrns\\.fr|we\\.tl)/[\\w\\-]+")) {
            br.setFollowRedirects(false);
            br.getPage(dlink);
            dlink = br.getRedirectLocation();
            if (dlink == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (dlink.contains("/error")) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
        }
        HASH = new Regex(dlink, "([0-9a-f]+)$").getMatch(0);
        CODE = new Regex(dlink, "wetransfer\\.com/downloads/([a-z0-9]+)/").getMatch(0);
        if (HASH == null || CODE == null) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }

        // Allow redirects for change to https
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        String recepientID = br.getRegex("data\\-recipient=\"([a-z0-9]+)\"").getMatch(0);
        if (recepientID == null) {
            recepientID = "";
        }
        String filesize = br.getRegex("<br>([^<>\"]*?)</div>[\t\n\r ]+<a href=\"#\" data\\-hash=").getMatch(0);
        final String mainpage = new Regex(dlink, "(https?://(www\\.)?([a-z0-9\\-\\.]+\\.)?wetransfer\\.com/)").getMatch(0);
        br.getPage(mainpage + "/api/v1/transfers/" + CODE + "/download?recipient_id=" + recepientID + "&security_hash=" + HASH + "&password=&ie=false&ts=" + System.currentTimeMillis());
        br.getRequest().setHtmlCode(br.toString().replace("\\", ""));
        if (br.containsHTML("\"error\":\"invalid_transfer\"")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        DLLINK = getJson("direct_link");
        if (DLLINK == null) {
            final String callback = br.getRegex("\"callback\":\"(\\{.*?)\"\\}\\}$").getMatch(0);
            String action = getJson("action");
            if (action == null || callback == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            action += "?";
            final String[] values = { "unique", "profile", "filename", "expiration", "escaped", "signature" };
            for (final String value : values) {
                final String result = getJson(value);
                if (result == null) {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                action += value + "=" + Encoding.urlEncode(result) + "&";
            }
            action += "callback=" + Encoding.urlEncode(callback);
            DLLINK = action;
        }
        if (DLLINK != null) {
            String filename = new Regex(Encoding.htmlDecode(DLLINK), "filename=\"([^<>\"]*?)\"").getMatch(0);
            if (filename == null) {
                filename = br.getRegex("\"filename\":\"([^<>\"]*?)\"").getMatch(0);
            }
            if (filename == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            link.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        } else {
            /** Old way */
            // AMF-Request
            br.getHeaders().put("Pragma", null);
            br.getHeaders().put("Accept-Charset", null);
            br.getHeaders().put("Accept", "*/*");
            br.getHeaders().put("Content-Type", "application/x-amf");
            br.getHeaders().put("Referer", "https://www.wetransfer.com/index.swf?nocache=" + String.valueOf(System.currentTimeMillis() / 1000));
            br.postPageRaw("https://v1.wetransfer.com/amfphp/gateway.php", getAMFRequest());

            /* TODO: remove me after 0.9xx public */
            br.getHeaders().put("Content-Type", null);

            // successfully request?
            final int rC = br.getHttpConnection().getResponseCode();
            if (rC != 200) {
                logger.warning("File not found! Link: " + dlink);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }

            final StringBuffer sb = new StringBuffer();
            /* CHECK: we should always use getBytes("UTF-8") or with wanted charset, never system charset! */
            for (final byte element : br.toString().getBytes()) {
                if (element < 127) {
                    if (element > 31) {
                        sb.append((char) element);
                    } else {
                        sb.append("#");
                    }
                }
            }
            final String result = sb.toString();
            if (new Regex(result, "(download_error_no_download|download_error_file_expired)").matches()) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }

            final String filename = new Regex(result, "#filename[#]+\\$?([^<>#]+)").getMatch(0);
            if (filesize == null) {
                filesize = new Regex(result, "#size[#]+(\\d+)[#]+").getMatch(0);
            }
            DLLINK = new Regex(result, "#awslink[#]+\\??([^<>#]+)").getMatch(0);

            if (filename == null || filesize == null || DLLINK == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }

            link.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        }
        if (filesize != null) {
            link.setDownloadSize(SizeFormatter.getSize(filesize));
        }

        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        // More chunks are possible for some links but not for all
        String fields = br.getRegex("\"fields\":\\{(\".*?\"\\]\\}\\}\")\\}\\}").getMatch(0);
        if (fields != null) {
            fields = fields.replace("\\\"", "JDTEMPREPLACEJD");
            final String[][] postData = new Regex(fields, "\"(.*?)\":\"(.*?)\"").getMatches();
            String postString = "";
            int counter = 0;
            for (String[] field : postData) {
                field[1] = field[1].replace("JDTEMPREPLACEJD", "\"");
                if (counter == 0) {
                    postString += field[0] + "=" + field[1];
                } else {
                    postString += "&" + field[0] + "=" + field[1];
                }
                counter++;
            }
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, postString, true, -2);
        } else {
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, true, -2);
        }
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("<title>Error while downloading your file")) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 5 * 60 * 1000l);
            }
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Unknown server error", 10 * 60 * 1000l);
        }
        dl.startDownload();
    }

    /**
     * Wrapper<br/>
     * Tries to return value of key from JSon response, from String source.
     *
     * @author raztoki
     * */
    private String getJson(final String source, final String key) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJson(source, key);
    }

    /**
     * Wrapper<br/>
     * Tries to return value of key from JSon response, from default 'br' Browser.
     *
     * @author raztoki
     * */
    private String getJson(final String key) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJson(br.toString(), key);
    }

    /**
     * Wrapper<br/>
     * Tries to return value of key from JSon response, from provided Browser.
     *
     * @author raztoki
     * */
    private String getJson(final Browser ibr, final String key) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJson(ibr.toString(), key);
    }

    /**
     * Wrapper<br/>
     * Tries to return value given JSon Array of Key from JSon response provided String source.
     *
     * @author raztoki
     * */
    private String getJsonArray(final String source, final String key) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJsonArray(source, key);
    }

    /**
     * Wrapper<br/>
     * Tries to return value given JSon Array of Key from JSon response, from default 'br' Browser.
     *
     * @author raztoki
     * */
    private String getJsonArray(final String key) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJsonArray(br.toString(), key);
    }

    /**
     * Wrapper<br/>
     * Tries to return String[] value from provided JSon Array
     *
     * @author raztoki
     * @param source
     * @return
     */
    private String[] getJsonResultsFromArray(final String source) {
        return jd.plugins.hoster.K2SApi.JSonUtils.getJsonResultsFromArray(source);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}