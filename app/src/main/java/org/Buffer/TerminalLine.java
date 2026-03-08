package org.Buffer;

import java.util.ArrayList;

/**
 * Represents a 'logical' line, i.e. the entire string of characters before a '\n' symbol
 * Stores both its CharacterCell string and how many lines it wraps over
 */
public class TerminalLine {
    ArrayList<CharacterCell> characters; //Stores the characters in this line
    Integer numLinesWrapped; //Stores how many physical lines this line wraps over

    /**
     * Creates a new Terminal line
     * Throws an IllegalArgumentException for negative values of capacity
     * @param capacity the initial capacity of the TerminalLine
     */
    public TerminalLine(int capacity) {
        characters = new ArrayList<>(capacity);
        numLinesWrapped = 1;
    }

    public Integer getNumLinesWrapped() {
        return this.numLinesWrapped;
    }
}
