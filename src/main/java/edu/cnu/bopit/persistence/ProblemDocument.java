package edu.cnu.bopit.persistence;

/** Versioned JSON root for a problem definition. */
public record ProblemDocument(FileHeader header, ProblemDto problem) { }
