package org.jdownloader.extensions.extraction.gui;

import javax.swing.Icon;

import jd.plugins.DownloadLink.AvailableStatus;

import org.appwork.swing.exttable.ExtTableModel;
import org.appwork.swing.exttable.columns.ExtTextColumn;
import org.jdownloader.extensions.extraction.DummyArchive;
import org.jdownloader.extensions.extraction.DummyArchiveFile;
import org.jdownloader.extensions.extraction.bindings.file.FileArchiveFile;
import org.jdownloader.extensions.extraction.translate.T;
import org.jdownloader.images.NewTheme;

public class DummyArchiveContentsTableModel extends ExtTableModel<DummyArchiveFile> {

    private ExtTextColumn<DummyArchiveFile> local;
    private ExtTextColumn<DummyArchiveFile> linkStatus;
    private ExtTextColumn<DummyArchiveFile> name;

    public ExtTextColumn<DummyArchiveFile> getLocal() {
        return local;
    }

    public DummyArchiveContentsTableModel(DummyArchive da) {
        super("DummyArchiveContentsTableModel");
        _fireTableStructureChanged(da.getList(), true);
    }

    @Override
    protected void initColumns() {
        addColumn(name = new ExtTextColumn<DummyArchiveFile>(T._.filename()) {

            @Override
            public String getStringValue(DummyArchiveFile value) {
                return value.getName();
            }

            @Override
            protected String getTooltipText(DummyArchiveFile value) {
                if (value.getArchiveFile() instanceof FileArchiveFile) {
                    if (((FileArchiveFile) value.getArchiveFile()).getFile().exists()) {
                        return T._.file_exists();
                    } else {
                        return T._.file_exists_not();
                    }
                } else {
                    if (value.getArchiveFile() == null) {
                        if (value.isMissing() || Boolean.TRUE.equals(value.isIncomplete())) {
                            return T._.file_exists_not();
                        }
                        return T._.unknown_tt();
                    } else {
                        if (value.isMissing() || Boolean.TRUE.equals(value.isIncomplete())) {
                            return T._.offline_tt();
                        }
                        if (value.getOnlineStatus() == AvailableStatus.TRUE) {
                            return T._.online_tt();
                        }
                        return T._.unknown_tt();
                    }
                }
            }

        });

        addColumn(linkStatus = new ExtTextColumn<DummyArchiveFile>(T._.exists()) {
            private Icon unknown;
            private Icon online;

            private Icon offline;

            {

                unknown = NewTheme.I().getIcon("help", 16);
                online = NewTheme.I().getIcon("true", 16);

                offline = NewTheme.I().getIcon("error", 16);
            }

            @Override
            protected Icon getIcon(DummyArchiveFile value) {
                if (value.getOnlineStatus() == AvailableStatus.TRUE) {
                    return online;
                }
                if (value.isMissing() || Boolean.TRUE.equals(value.isIncomplete())) {
                    return offline;
                }
                return unknown;
            }

            @Override
            public String getStringValue(DummyArchiveFile value) {
                if (value.getOnlineStatus() == AvailableStatus.TRUE) {
                    return T._.online();
                }
                if (value.isMissing() || Boolean.TRUE.equals(value.isIncomplete())) {
                    return T._.offline();
                }
                return T._.unknown();
            }

            @Override
            protected String getTooltipText(DummyArchiveFile value) {
                if (value.getOnlineStatus() == AvailableStatus.TRUE) {
                    return T._.online_tt();
                }
                if (value.isMissing() || Boolean.TRUE.equals(value.isIncomplete())) {
                    return T._.offline_tt();
                }
                return T._.unknown_tt();
            }

        });

        addColumn(local = new ExtTextColumn<DummyArchiveFile>(T._.local()) {

            @Override
            protected Icon getIcon(DummyArchiveFile value) {
                if (value.isLocalFileAvailable()) {
                    return NewTheme.I().getIcon("true", 16);
                }
                return NewTheme.I().getIcon("false", 16);

            }

            @Override
            public String getStringValue(DummyArchiveFile value) {
                if (value.isLocalFileAvailable()) { //
                    return T._.downloadedok();
                }
                return T._.downloadedbad();
            }

            @Override
            protected String getTooltipText(DummyArchiveFile value) {
                return getStringValue(value);
            }

        });
    }

    public ExtTextColumn<DummyArchiveFile> getLinkStatus() {
        return linkStatus;
    }

    public ExtTextColumn<DummyArchiveFile> getName() {
        return name;
    }

}
