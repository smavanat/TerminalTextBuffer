package org.Buffer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a terminal line, i.e. the entire string of characters up to its parent screen buffer's width
 * Stores both its CharacterCell string and whether it wraps from the previous line
 */
public class TerminalLine {
    ArrayList<CharacterCell> characters; //Stores the characters in this line
    private int width; //The max width this line can stretch to
    private boolean wrapped; //Whether this line wraps from the previous one

    /**
     * Creates a new Terminal line
     * Throws an IllegalArgumentException for negative values of capacity
     * @param width the width of the TerminalLine
     */
    public TerminalLine(int width, boolean wrapped) {
        if(width < 0) throw new IllegalArgumentException();

        characters = new ArrayList<>(width);
        this.width = width;
        this.wrapped = wrapped;
    }

    /**
     * Default constructor for an unwrapped line
     * @param width the width of the new TerminalLine
     */
    public TerminalLine(int width) {
        this(width, false);
    }

    /**
     * @return whether this line wraps from the previous one
     */
    public boolean getWrapped() {
        return this.wrapped;
    }
    /**
     * @return the internal CharacterCell this object stores
     */
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
        if(index < 0 || index > characters.size())
            throw new IndexOutOfBoundsException();

        if(characters.size() < width)
            characters.add(index, cell);
    }

    /**
     * Adds a new character cell at a specific index
     * @param cell the new cell to add
     */
    public void add(CharacterCell cell) {
        if(characters.size() < width)
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

    /**
     * Wrapper for {@link ArrayList}.addAll()
     */
    public void addAll(Collection<? extends CharacterCell> c) {
        characters.addAll(c);
    }

    /**
     * Wrapper for {@link ArrayList}.remove()
     * @param index the index of the element to be removed
     */
    public void remove(int index) {
        characters.remove(index);
    }
}
