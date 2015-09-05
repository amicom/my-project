/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components;

import java.awt.Color;

/**
 * @author Thomas
 * 
 */
public interface TextComponentInterface {
    String getText();

    void setText(String text);
    String getHelpText();
    void setHelpText(final String helpText);
//    Color getHelpColor();
    void setHelpColor(Color color);
    void onChanged();

    /**
     * 
     */
    void selectAll();

    /**
     * 
     */
    boolean requestFocusInWindow();
    
    
}
