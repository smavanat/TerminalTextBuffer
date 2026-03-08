package org.Buffer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a 'logical' line, i.e. the entire string of characters before a '\n' symbol
 * Stores both its CharacterCell string and how many lines it wraps over
 */
public class TerminalLine {
    ArrayList<CharacterCell> characters; //Stores the characters in this line
    private int width;
    private boolean wrapped;

    /**
     * Creates a new Terminal line
     * Throws an IllegalArgumentException for negative values of capacity
     * @param width the width of the TerminalLine
     */
    public TerminalLine(int width, boolean wrapped) {
        characters = new ArrayList<>(width);
        this.width = width;
        this.wrapped = wrapped;
    }

    public TerminalLine(int width) {
        this(width, false);
    }

    public boolean getWrapped() {
        return this.wrapped;
    }
    public int getWidth() {
        return this.width;
    }
    public ArrayList<CharacterCell> getCharacters() {
        return this.characters;
    }

    /**
     * Gets the character cell at a specific index
     * Throws an IndexOutOfBoundsException if the given index is not in the range [0, size)
     * @param index the index of the character cell we want to retrieve
     * @return the character cell at the provided index
     */
    public CharacterCell get(int index) {
        if(index < 0 || index >= characters.size())
            throw new IndexOutOfBoundsException();

        return characters.get(index);
    }

    /**
     * Adds a new character cell at a specific index
     * Throws an IndexOutOfBoundsException if the given index is not in the range [0, size)
     * @param index the index where the new cell should be added
     * @param cell the new cell to add
     */
    public void add(int index, CharacterCell cell) {
        if(index < 0 || index >= characters.size())
            throw new IndexOutOfBoundsException();

        characters.add(index, cell);
    }

    /**
     * Adds a new character cell at a specific index
     * @param cell the new cell to add
     */
    public void add(CharacterCell cell) {
        if(characters.size() <= width)
            characters.add(cell);
    }

    /**
     * @return the number of characters in this line
     */
    public int size() {
        return this.characters.size();
    }

    /**
     * Clears the characters stored in this {@link TerminalLine}
     */
    public void clear() {
        characters.clear();
    }

    public void addAll(Collection<? extends CharacterCell> c) {
        characters.addAll(c);
    }
}
