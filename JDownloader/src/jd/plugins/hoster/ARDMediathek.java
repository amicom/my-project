//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.nutils.encoding.HTMLEntities;
import jd.parser.Regex;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision: 31424 $", interfaceVersion = 2, names = { "ard.de" }, urls = { "http://ardmediathekdecrypted/\\d+" }, flags = { 32 })
public class ARDMediathek extends PluginForHost {

    private static final String Q_LOW                 = "Q_LOW";
    private static final String Q_MEDIUM              = "Q_MEDIUM";
    private static final String Q_HIGH                = "Q_HIGH";
    private static final String Q_HD                  = "Q_HD";
    private static final String Q_BEST                = "Q_BEST";
    private static final String Q_HTTP_ONLY           = "Q_HTTP_ONLY_3";
    private static final String AUDIO                 = "AUDIO";
    private static final String Q_SUBTITLES           = "Q_SUBTITLES";
    private static final String FASTLINKCHECK         = "FASTLINKCHECK";

    private static final String EXCEPTION_LINKOFFLINE = "EXCEPTION_LINKOFFLINE";

    private String              DLLINK                = null;

    public ARDMediathek(final PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
    }

    @Override
    public String getAGBLink() {
        return "http://www.ardmediathek.de/ard/servlet/content/3606532";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws IOException, PluginException, GeneralSecurityException {
        DLLINK = null;
        if (downloadLink.getBooleanProperty("offline", false)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        /* Load this plugin as we use functions of it. */
        JDUtilities.getPluginForHost("br-online.de");
        if (downloadLink.getStringProperty("directURL", null) == null) {
            /* Undefined case! */
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String finalName = downloadLink.getStringProperty("directName", null);
        if (finalName == null) {
            finalName = getTitle(br) + ".mp4";
        }
        downloadLink.setFinalFileName(finalName);
        if (!downloadLink.getStringProperty("directURL").startsWith("http")) {
            return AvailableStatus.TRUE;
        }
        // get filesize
        final Browser br2 = br.cloneBrowser();
        br2.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            br2.getHeaders().put("Accept-Encoding", "identity");
            /* TODO: Remove this compatibility workaround AFTER 2015-10 */
            DLLINK = downloadLink.getStringProperty("directURL");
            if (DLLINK.contains("@")) {
                DLLINK = DLLINK.split("@")[0];
            }
            con = br2.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                downloadLink.setDownloadSize(con.getLongContentLength());
            } else {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
            }
        }
    }

    private String getTitle(Browser br) {
        String title = br.getRegex("<div class=\"MainBoxHeadline\">([^<]+)</").getMatch(0);
        String titleUT = br.getRegex("<span class=\"BoxHeadlineUT\">([^<]+)</").getMatch(0);
        if (title == null) {
            title = br.getRegex("<title>ard\\.online \\- Mediathek: ([^<]+)</title>").getMatch(0);
        }
        if (title == null) {
            title = br.getRegex("<h2>(.*?)</h2>").getMatch(0);
        }
        if (title != null) {
            title = Encoding.htmlDecode(title + (titleUT != null ? "__" + titleUT.replaceAll(":$", "") : "").trim());
        }
        if (title == null) {
            title = "UnknownTitle_" + System.currentTimeMillis();
        }
        return title;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception {
        requestFileInformation(downloadLink);
        download(downloadLink);
    }

    private void setupRTMPConnection(String[] stream, DownloadInterface dl) {
        jd.network.rtmp.url.RtmpUrlConnection rtmp = ((RTMPDownload) dl).getRtmpConnection();
        rtmp.setPlayPath(stream[1]);
        rtmp.setUrl(stream[0]);
        rtmp.setResume(true);
        rtmp.setTimeOut(10);
    }

    private void download(final DownloadLink downloadLink) throws Exception {
        if (downloadLink.getStringProperty("directURL", null) == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        if (DLLINK.startsWith("rtmp")) {
            /* rtmp = unsupported */
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // dl = new RTMPDownload(this, downloadLink, DLLINK);
            // setupRTMPConnection(null, dl);
            // ((RTMPDownload) dl).startDownload();
        } else {
            br.setFollowRedirects(true);
            if (DLLINK == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (DLLINK.startsWith("mms")) {
                throw new PluginException(LinkStatus.ERROR_FATAL, "Protocol (mms://) not supported!");
            }
            // Workaround to avoid DOWNLOAD INCOMPLETE errors
            boolean resume = true;
            int maxChunks = 0;
            if ("subtitle".equals(downloadLink.getStringProperty("streamingType", null))) {
                br.getHeaders().put("Accept-Encoding", "identity");
                downloadLink.setDownloadSize(0);
                resume = false;
                maxChunks = 1;
            }
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, resume, maxChunks);
            if (dl.getConnection().getContentType().contains("html")) {
                br.followConnection();
                if (dl.getConnection().getResponseCode() == 403) {
                    throw new PluginException(LinkStatus.ERROR_FATAL, "This Content is not longer available!");
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (this.dl.startDownload()) {
                this.postprocess(downloadLink);
            }
        }
    }

    private void postprocess(final DownloadLink downloadLink) {
        if ("subtitle".equals(downloadLink.getStringProperty("streamingType", null))) {
            if (!convertSubtitle(downloadLink)) {
                logger.severe("Subtitle conversion failed!");
            } else {
                downloadLink.setFinalFileName(downloadLink.getStringProperty("directName", null).replace(".xml", ".srt"));
            }
        }
    }

    private String getMainlink(final DownloadLink dl) {
        return dl.getStringProperty("mainlink", null);
    }

    /**
     * Converts the ARD Closed Captions subtitles to SRT subtitles. It runs after the completed download.
     *
     * @return The success of the conversion.
     */
    private boolean convertSubtitle(final DownloadLink downloadlink) {
        final File source = new File(downloadlink.getFileOutput());

        final StringBuilder xml = new StringBuilder();
        int counter = 1;
        final String lineseparator = System.getProperty("line.separator");

        Scanner in = null;
        try {
            in = new Scanner(new FileReader(source));
            while (in.hasNext()) {
                xml.append(in.nextLine() + lineseparator);
            }
        } catch (Exception e) {
            return false;
        } finally {
            in.close();
        }
        boolean success;
        final String xmlContent = xml.toString();
        /* They got two different subtitle formats */
        if (xmlContent.contains("<ebuttm:documentEbuttVersion>")) {
            success = jd.plugins.hoster.BrOnlineDe.convertSubtitleBrOnlineDe(downloadlink, xmlContent, 0);
        } else {
            BufferedWriter dest;
            try {
                dest = new BufferedWriter(new FileWriter(new File(source.getAbsolutePath().replace(".xml", ".srt"))));
            } catch (IOException e1) {
                return false;
            }

            final String[] matches = new Regex(xmlContent, "(<p id=\"subtitle\\d+\".*?</p>)").getColumn(0);
            try {
                /* Find style --> color assignments */
                final HashMap<String, String> styles_color_names = new HashMap<String, String>();
                final String[] styles = new Regex(xmlContent, "(<style id=\"s\\d+\".*?/>)").getColumn(0);
                if (styles != null) {
                    for (final String style_info : styles) {
                        final String style_id = new Regex(style_info, "<style id=\"(s\\d+)\"").getMatch(0);
                        final String style_color = new Regex(style_info, "tts:color=\"([a-z]+)\"").getMatch(0);
                        if (style_id != null && style_color != null) {
                            styles_color_names.put(style_id, style_color);
                        }
                    }
                }
                styles_color_names.put("s1", "black");

                for (final String info : matches) {
                    dest.write(counter++ + lineseparator);
                    final DecimalFormat df = new DecimalFormat("00");
                    final Regex startInfo = new Regex(info, "begin=\"(\\d{2}):([^<>\"]*?)\"");
                    final Regex endInfo = new Regex(info, "end=\"(\\d{2}):([^<>\"]*?)\"");
                    int startHour = Integer.parseInt(startInfo.getMatch(0));
                    int endHour = Integer.parseInt(endInfo.getMatch(0));
                    startHour -= 10;
                    endHour -= 10;
                    final String start = df.format(startHour) + ":" + startInfo.getMatch(1).replace(".", ",");
                    final String end = df.format(endHour) + ":" + endInfo.getMatch(1).replace(".", ",");
                    dest.write(start + " --> " + end + lineseparator);

                    final String[][] color_texts = new Regex(info, "style=\"(s\\d+)\">?(.*?)</p>").getMatches();
                    String text = "";
                    for (final String[] style_text : color_texts) {
                        final String style = style_text[0];
                        text = style_text[1];
                        text = text.replaceAll(lineseparator, " ");
                        text = text.replaceAll("&apos;", "\\\\u0027");
                        text = unescape(text);
                        text = HTMLEntities.unhtmlentities(text);
                        text = HTMLEntities.unhtmlAmpersand(text);
                        text = HTMLEntities.unhtmlAngleBrackets(text);
                        text = HTMLEntities.unhtmlSingleQuotes(text);
                        text = HTMLEntities.unhtmlDoubleQuotes(text);
                        text = text.replaceAll("<br />", lineseparator);
                        text = text.replaceAll("</?(p|span)>?", "");
                        text = text.trim();
                        final String remove_color = new Regex(text, "( ?tts:color=\"[a-z0-9]+\">)").getMatch(0);
                        if (remove_color != null) {
                            text = text.replace(remove_color, "");
                        }

                        final String color = styles_color_names.get(style);
                        final String color_code = getColorCode(color);
                        text = "<font color=#" + color_code + ">" + text + "</font>";

                    }
                    dest.write(text + lineseparator + lineseparator);
                }
                success = true;
            } catch (final Exception e) {
                success = false;
            } finally {
                try {
                    dest.close();
                } catch (IOException e) {
                }
            }
            source.delete();
        }

        return success;
    }

    private static String getColorCode(final String colorName) {
        /* Unhandled case/standard = white */
        String colorCode;
        if (colorName == null) {
            /* Use black for missing/not used/given colors */
            colorCode = "FFFFFF";
        } else if (colorName.equals("blue")) {
            colorCode = "0000FF";
        } else if (colorName.equals("yellow")) {
            colorCode = "FFFF00";
        } else if (colorName.equals("aqua")) {
            colorCode = "00FFFF";
        } else if (colorName.equals("lime")) {
            colorCode = "00FF00";
        } else if (colorName.equals("fuchsia")) {
            colorCode = "FF00FF";
        } else if (colorName.equals("green")) {
            colorCode = "008000";
        } else if (colorName.equals("cyan")) {
            colorCode = "00FFFF";
        } else {
            colorCode = "FFFFFF";
        }
        return colorCode;
    }

    private static AtomicBoolean yt_loaded = new AtomicBoolean(false);

    private String unescape(final String s) {
        /* we have to make sure the youtube plugin is loaded */
        if (!yt_loaded.getAndSet(true)) {
            JDUtilities.getPluginForHost("youtube.com");
        }
        return jd.plugins.hoster.Youtube.unescape(s);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

    @Override
    public void resetPluginGlobals() {
    }

    private boolean isEmpty(final String ip) {
        return ip == null || ip.trim().length() == 0;
    }

    @Override
    public String getDescription() {
        return "JDownloader's ARD Plugin helps downloading videoclips from ardmediathek.de and daserste.de. You can choose between different video qualities.";
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_SUBTITLES, JDL.L("plugins.hoster.ardmediathek.subtitles", "Untertitel herunterladen")).setDefaultValue(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Video settings: "));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        final ConfigEntry bestonly = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_BEST, JDL.L("plugins.hoster.ard.best", "Immer nur die bestmöglichste Qualitätsstufe herunterladen?")).setDefaultValue(false);
        getConfig().addEntry(bestonly);
        /* Disabled because not needed at the moment (rtmp streams) */
        // getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_HTTP_ONLY,
        // JDL.L("plugins.hoster.ard.best", "Only download HTTP streams (avoid [RTMP] versions)")).setDefaultValue(true).setEnabled(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_LOW, JDL.L("plugins.hoster.ard.loadlow", "low version herunterladen")).setDefaultValue(true).setEnabledCondidtion(bestonly, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_MEDIUM, JDL.L("plugins.hoster.ard.loadmedium", "medium version herunterladen")).setDefaultValue(true).setEnabledCondidtion(bestonly, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_HIGH, JDL.L("plugins.hoster.ard.loadhigh", "high version herunterladen")).setDefaultValue(true).setEnabledCondidtion(bestonly, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), Q_HD, JDL.L("plugins.hoster.ard.loadhd", "HD version herunterladen")).setDefaultValue(true).setEnabledCondidtion(bestonly, false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "For Dossier links:"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        /* Disabled because not possible at the moment (audio content) */
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), AUDIO, JDL.L("plugins.hoster.ard.audio", "Audio Inhalte herunterladen")).setDefaultValue(false).setEnabled(false));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), FASTLINKCHECK, JDL.L("plugins.hoster.ard.fastlinkcheck", "Aktiviere schnellen Linkcheck?\r\nFalls aktiv: Dateigrößen sind erst beim Downloadstart sichtbar!")).setDefaultValue(false));
    }

}