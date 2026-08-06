package edu.cnu.bopit.persistence;

/** A malformed, unsupported, or unreadable BOPIT persistence document. */
public final class PersistenceException extends Exception {
    public PersistenceException(String message) { super(message); }
    public PersistenceException(String message, Throwable cause) { super(message, cause); }
}
