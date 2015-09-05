//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
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

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForDecrypt;

@DecrypterPlugin(revision = "$Revision: 25440 $", interfaceVersion = 2, names = { "newalbumreleases.net" }, urls = { "http://(www\\.)?newalbumreleases\\.net/[A-Za-z0-9=/]+" }, flags = { 0 })
public class NwLbmRlsesNet extends PluginForDecrypt {

    public NwLbmRlsesNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    private static final String INVALIDLINKS = "http://(www\\.)?newalbumreleases\\.net/(date/[0-9/]+|category/.+|about/?|comments/feed/?|feed/?|page.+)";

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        if (parameter.matches(INVALIDLINKS)) {
            logger.info("Link invalid: " + parameter);
            return decryptedLinks;
        }
        if (parameter.matches("http://(www\\.)?newalbumreleases\\.net/\\d+(/?.+)?")) {
            br.setFollowRedirects(true);
            br.getPage(parameter);
            if (br.getHttpConnection().getResponseCode() == 404) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            br.setFollowRedirects(false);
            final String[] links = br.getRegex("\"(https?://[^<>\"]*?)\">DOWNLOAD</a>").getColumn(0);
            if (links == null || links.length == 0) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            for (final String singleLink : links) {
                String finallink;
                if (singleLink.contains("newalbumreleases.net/")) {
                    br.getPage(singleLink);
                    finallink = br.getRedirectLocation();
                    if (finallink == null) {
                        logger.warning("Decrypter broken for link: " + parameter);
                        return null;
                    }
                } else {
                    finallink = singleLink;
                }
                decryptedLinks.add(createDownloadlink(finallink));
            }
        } else {
            br.setFollowRedirects(false);
            br.getPage(parameter);
            if (br.getHttpConnection().getResponseCode() == 404) {
                logger.info("Link offline: " + parameter);
                return decryptedLinks;
            }
            final String finallink = br.getRedirectLocation();
            if (finallink == null) {
                logger.warning("Decrypter broken for link: " + parameter);
                return null;
            }
            decryptedLinks.add(createDownloadlink(finallink));
        }

        return decryptedLinks;
    }

}
