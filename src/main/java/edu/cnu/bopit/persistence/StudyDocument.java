package edu.cnu.bopit.persistence;

/** Versioned JSON root for a study and optional retained result. */
public record StudyDocument(FileHeader header, StudyDto study, StudyResultDto result) { }
