package edu.cnu.bopit.ui;

import edu.cnu.mdi.dialog.FileType;

/** Shared file-type definitions for BOPIT persistence and exports. */
public final class BopitFileTypes {
    public static final FileType JSON = FileType.of("BOPIT JSON files", "json");
    public static final FileType TEXT = FileType.of("Text files", "txt");
    public static final FileType CSV = FileType.of("CSV files", "csv");

    private BopitFileTypes() { }
}
