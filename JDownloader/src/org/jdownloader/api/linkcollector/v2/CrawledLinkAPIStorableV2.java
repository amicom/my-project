package org.jdownloader.api.linkcollector.v2;

import jd.controlling.linkcrawler.CrawledLink;

import org.appwork.storage.SimpleMapper;
import org.appwork.storage.Storable;
import org.jdownloader.myjdownloader.client.bindings.linkgrabber.CrawledLinkStorable;

public class CrawledLinkAPIStorableV2 extends CrawledLinkStorable implements Storable {
    public static void main(String[] args) {
        System.out.println(new SimpleMapper().objectToString(new CrawledLinkAPIStorableV2()));
    }

    public CrawledLinkAPIStorableV2(/* Storable */) {

    }

    public CrawledLinkAPIStorableV2(CrawledLink link) {
        setName(link.getName());
        setUuid(link.getUniqueID().getID());
    }

}
