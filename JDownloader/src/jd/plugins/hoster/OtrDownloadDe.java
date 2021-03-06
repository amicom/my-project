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
import jd.config.Property;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision: 19295 $", interfaceVersion = 2, names = { "otr-download.de" }, urls = { "http://(www\\.)?otr\\-download\\.de/(download\\.php)?\\?file=[^<>\"\\']+\\.otrkey" }, flags = { 0 })
public class OtrDownloadDe extends PluginForHost {

    public OtrDownloadDe(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public String getAGBLink() {
        return "http://otr-download.de/index.php?s=impressum&session=";
    }

    public void correctDownloadLink(DownloadLink link) {
        if (!link.getDownloadURL().contains("download.php")) link.setUrlDownload(link.getDownloadURL().replace("?file=", "download.php?file="));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCustomCharset("utf-8");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("die angeforderte Datei konnte nicht gefunden werden|>Angeforderte Datei nicht zum Download")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final String filename = new Regex(link.getDownloadURL(), "\\?file=(.+)").getMatch(0);
        final String filesize = br.getRegex("<small>Größe: </small>([^<>\"]*?)</li>").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String dllink = downloadLink.getStringProperty("freelink");
        if (dllink != null) {
            try {
                Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(dllink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    downloadLink.setProperty("freelink", Property.NULL);
                    dllink = null;
                }
                con.disconnect();
            } catch (Exception e) {
                downloadLink.setProperty("freelink", Property.NULL);
                dllink = null;
            }
        }
        if (dllink == null) {
            br.getPage("http://otr-download.de/wait.php?action=insert&file=" + downloadLink.getName());
            String waitPosition = null;
            for (int i = 1; i <= 60; i++) {
                br.getPage("http://otr-download.de/wait.php");
                dllink = br.getRegex("\"(download\\.php\\?action=waitDownload\\&warteHash=[a-z0-9]+)\"").getMatch(0);
                if (dllink != null) break;
                waitPosition = br.getRegex("<strong>Position:</strong> (\\d+)").getMatch(0);
                if (waitPosition != null) {
                    downloadLink.getLinkStatus().setStatusText("Momentane Warteposition: " + waitPosition);
                    System.out.println("Posi: " + waitPosition);
                }
                sleep(60 * 1000l, downloadLink);
            }
            if (dllink == null && waitPosition != null)
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Zu lange in der Warteschleife", 30 * 60 * 1000l);
            else if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            dllink = "http://otr-download.de/" + dllink;
        }
        br.getPage(dllink);
        dllink = br.getRegex("<small>Highspeed Download</small>[\t\n\r ]+<a href=\"(http://[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\"(http://s\\d+\\.otr\\-download\\.de:\\d+/[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("freelink", dllink);
        dl.startDownload();
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 4;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}