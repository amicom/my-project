/**
 * Copyright (c) 2009 - 2015 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 *
 * This file is part of org.appwork.utils.os.mime
 *
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.os.mime;

import org.appwork.utils.os.CrossSystem;

/**
 * @author daniel
 *
 */
public class MimeFactory {

    public static Mime getInstance() {
        if (CrossSystem.isWindows()) {
            return new MimeWindows();
        } else if (CrossSystem.isLinux()) {
            return new MimeLinux();
        } else if (CrossSystem.isMac()) {
            return new MimeMac();
        } else {
            return new MimeDefault();
        }
    }
}
