package edu.cnu.bopit.model;

/** Common immutable specification for supported momentum-grid strategies. */
public sealed interface GridSpec permits LegacyGridSpec, AdaptiveGridSpec {
    int totalPoints();
}
