package org.Buffer;

/**
 * Enum to keep the state of a CharacterCell
 */
public enum TrailFlag {
    /**
     * A one-width char
     */
    NORMAL,
    /**
     * The start of a 2-width char
     */
    WIDE_START,
    /**
     * The end of a 2-width char
     */
    WIDE_END
}
