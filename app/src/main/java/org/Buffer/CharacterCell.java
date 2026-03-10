package org.Buffer;

import java.util.Arrays;

/**
 * Class to represent a single character cell within the terminal.
 * Consists of:
 *      a {@link Character} representing the character in the cell
 *      a {@link Colour} representing the foreground colour of the cell
 *      a {@link Colour} representing the background colour of the cell
 *      a {@link Style} representing the style flag of the cell
 */
public class CharacterCell {
    private Character character;
    private Colour foregroundColour;
    private Colour backgroundColour;
    private boolean styleFlags[];
    private TrailFlag trailFlag;

    /**
     * Constructor for a CharacterCell
     * @param c the character to be stored in this cell
     * @param fc the foreground colour of the cell
     * @param bc the background colour of the cell
     * @param sf the style flag of the cell
     * @param tf the type of this character (1 space, 2 space start, 2 space end)
     */
    public CharacterCell(Character c, Colour fc, Colour bc, boolean sf[], TrailFlag tf) {
        this.character = c;
        this.foregroundColour= fc;
        this.backgroundColour = bc;
        this.styleFlags = sf;
        this.trailFlag = tf;
    }

    /**
     * Constructor for a CharacterCell where the trailer flag is automatically set to false.
     * Should be the default constructor for non-wide characters
     * @param c the character to be stored in this cell
     * @param fc the foreground colour of the cell
     * @param bc the background colour of the cell
     * @param sf the style flag of the cell
     */
    public CharacterCell(Character c, Colour fc, Colour bc, boolean sf[]) {
        this(c, fc, bc, sf, TrailFlag.NORMAL);
    }

    /**
     * Constructor for a non-wide CharacterCell where all other attributes are set to default values, i.e. DEFAULT, DEFAULT and NONE
     * @param c the character this CharacterCell should contain
     */
    public CharacterCell(Character c) {
        this(c, Colour.DEFAULT, Colour.DEFAULT, new boolean[]{false,false,false});
    }

    /**
     * Constructor for a wide CharacterCell where all other attributes are set to default values, i.e. DEFAULT, DEFAULT and NONE
     * @param c the character this CharacterCell should contain
     * @param tf the type of this character (1 space, 2 space start, 2 space end)
     */
    public CharacterCell(Character c, TrailFlag tf) {
        this(c, Colour.DEFAULT, Colour.DEFAULT, new boolean[]{false, false, false}, tf);
    }

    /**
     * @return the character stored in this CharacterCell
     */
    public Character getCharacter() {
        return this.character;
    }
    /**
     * @param val the value to set this CharacterCell's character to
     */
    public void setCharacter(Character val) {
        this.character = val;
    }

    /**
     * @return the foreground colour stored in this CharacterCell
     */
    public Colour getForegroundColour() {
        return this.foregroundColour;
    }
    /**
     * @param val the value to set this CharacterCell's foreground colour to
     */
    public void setForegroundColour(Colour val) {
        this.foregroundColour = val;
    }

    /**
     * @return the background colour stored in this CharacterCell
     */
    public Colour getBackgroundColour() {
        return this.backgroundColour;
    }
    /**
     * @param val the value to set this CharacterCell's background colour to
     */
    public void setBackgroundColour(Colour val) {
        this.backgroundColour = val;
    }

    /**
     * @param flag the style flag whose status you want to know
     * @return the status of the given flag in this CharacterCell
     */
    public boolean getStyleFlag(Style flag) {
        return this.styleFlags[flag.ordinal()];
    }
    /**
     * Sets the provided style flag for this char to the given value
     * @param flag the style flag to set
     * @param val the value to set this Style to
     */
    public void setStyleFlag(Style flag, boolean val) {
        this.styleFlags[flag.ordinal()] = val;
    }

    /**
     * @return the trial flag stored in this CharacterCell
     */
    public TrailFlag getTrailFlag() {
        return this.trailFlag;
    }
    /**
     * @param val the value to set this CharacterCell's trail flag to
     */
    public void setTrailFlag(TrailFlag val) {
        this.trailFlag = val;
    }

    /**
     * @return the state of all the style flags stored in this CharacterCell
     */
    public boolean[] getAllStyleFlags() {
        return this.styleFlags;
    }
    /**
     * @param vals the value to set this CharacterCell's style flags to
     */
    public void setAllStyleFlags(boolean vals[]) {
        this.styleFlags = vals;
    }

    /**
     * Checks if two CharacterCells are equal by checking for equality between all constituent variables
     * @param o the other {@link Object} to check against
     * @return true if all internal are the same, false if not or if the provided object is null or of a different class
     */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;

        CharacterCell other = (CharacterCell) o;

        if(this.character.equals(other.getCharacter()) &&
            this.foregroundColour.equals(other.getForegroundColour()) &&
            this.backgroundColour.equals(other.getBackgroundColour()) &&
            Arrays.equals(this.styleFlags, other.getAllStyleFlags())) return true;

        return false;
    }

    /**
     * Copies all of the internal variables of a different {@link CharacterCell} into the caller
     * @param c the CharacterCell to copy from
     */
    public void copy(CharacterCell c) {
        this.character = c.getCharacter();
        this.backgroundColour = c.getBackgroundColour();
        this.foregroundColour = c.getForegroundColour();
        this.styleFlags = c.getAllStyleFlags();
        this.trailFlag = c.getTrailFlag();
    }
}
