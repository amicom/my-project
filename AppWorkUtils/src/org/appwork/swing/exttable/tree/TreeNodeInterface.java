/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.tree;

import java.util.List;

/**
 * @author Thomas
 * 
 */
public interface TreeNodeInterface {
    boolean isLeaf();

    List<TreeNodeInterface> getChildren();

    public TreeNodeInterface getParent();

    public void setParent(TreeNodeInterface parent);
}
