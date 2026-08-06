package edu.cnu.bopit.persistence;

import java.time.Instant;

/** Common identity and compatibility information for every BOPIT JSON file. */
public record FileHeader(String format, int schemaVersion,
        String applicationVersion, Instant created) {
    public FileHeader {
        if (format == null || format.isBlank() || schemaVersion < 1
                || applicationVersion == null || applicationVersion.isBlank() || created == null) {
            throw new IllegalArgumentException("invalid BOPIT file header");
        }
    }
}
