package org.jdownloader.gui.views.components.packagetable.columns;

import javax.swing.JPopupMenu;

import jd.controlling.linkcrawler.CrawledLink;
import jd.controlling.linkcrawler.CrawledPackage;
import jd.controlling.packagecontroller.AbstractNode;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.appwork.utils.StringUtils;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.downloads.columns.FileColumn;

public class ChecksumColumn extends ExtTextColumn<AbstractNode> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ChecksumColumn() {
        super(_GUI._.checksumcolumnmd5());

    }

    public JPopupMenu createHeaderPopup() {

        return FileColumn.createColumnPopup(this, getMinWidth() == getMaxWidth() && getMaxWidth() > 0);

    }

    public void configureEditorComponent(final AbstractNode value, final boolean isSelected, final int row, final int column) {

        this.editorField.removeActionListener(this);
        String str = this.getEditorStringValue(value);
        if (str == null) {
            // under substance, setting setText(null) somehow sets the label
            // opaque.
            str = "";
        }
        this.editorField.setText(str);
        this.editorField.addActionListener(this);

        this.editorIconLabel.setIcon(this.getIcon(value));

    }

    private String getEditorStringValue(AbstractNode value) {
        final DownloadLink dl;
        if (value instanceof CrawledLink) {
            dl = ((CrawledLink) value).getDownloadLink();
        } else if (value instanceof DownloadLink) {
            dl = ((DownloadLink) value);
        } else {
            return null;
        }
        if (dl != null) {
            String hash = dl.getSha256Hash();
            if (!StringUtils.isEmpty(hash)) {
                return hash;
            }
            hash = dl.getSha1Hash();
            if (!StringUtils.isEmpty(hash)) {
                return hash;
            }
            hash = dl.getMD5Hash();
            if (!StringUtils.isEmpty(hash)) {
                return hash;
            }
        }
        return null;
    }

    @Override
    public boolean isDefaultVisible() {
        return false;
    }

    // @Override
    // public boolean isEditable(final E obj) {
    // return false;

    @Override
    protected boolean isEditable(final AbstractNode obj, final boolean enabled) {
        return isEditable(obj);
    }

    @Override
    public boolean isEditable(AbstractNode obj) {
        if (obj instanceof CrawledLink) {
            return true;
        }
        if (obj instanceof DownloadLink) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEnabled(final AbstractNode obj) {
        if (obj instanceof CrawledPackage) {
            return ((CrawledPackage) obj).getView().isEnabled();
        }
        if (obj instanceof FilePackage) {
            return ((FilePackage) obj).getView().isEnabled();
        }
        return obj.isEnabled();
    }

    @Override
    protected void setStringValue(String value, AbstractNode object) {
        DownloadLink dl = null;
        if (object instanceof CrawledLink) {
            dl = ((CrawledLink) object).getDownloadLink();
        } else if (object instanceof DownloadLink) {
            dl = ((DownloadLink) object);
        }
        if (dl != null) {
            value = value.trim();
            if (value != null && value.length() == 32) {
                dl.setMD5Hash(value);
            } else if (value != null && value.length() == 40) {
                dl.setSha1Hash(value);
            } else if (value != null && value.length() == 64) {
                dl.setSha256Hash(value);
            } else if (value == null || value.length() == 0) {
                dl.setSha1Hash(null);
                dl.setSha256Hash(null);
                dl.setMD5Hash(null);
            }
        }
    }

    @Override
    public String getStringValue(AbstractNode value) {
        final DownloadLink dl;
        if (value instanceof CrawledLink) {
            dl = ((CrawledLink) value).getDownloadLink();
        } else if (value instanceof DownloadLink) {
            dl = ((DownloadLink) value);
        } else {
            return null;
        }
        if (dl != null) {
            String hash = dl.getSha256Hash();
            if (!StringUtils.isEmpty(hash)) {
                return "[SHA256] ".concat(hash);
            }
            hash = dl.getSha1Hash();
            if (!StringUtils.isEmpty(hash)) {
                return "[SHA1] ".concat(hash);
            }
            hash = dl.getMD5Hash();
            if (!StringUtils.isEmpty(hash)) {
                return "[MD5] ".concat(hash);
            }
        }
        return null;
    }

}
