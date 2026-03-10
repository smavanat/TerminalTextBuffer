package org.Buffer;

import java.util.Arrays;

/**
 * Class to represent a single character cell within the terminal.
 * Consists of:
 *      a {@link Character} representing the character in the cell
 *      a {@link Colour} representing the foreground colour of the cell
 *      a {@link Colour} representing the background colour of the cell
 *      a {@link Flag} representing the style flag of the cell
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

    //Getters and setters for all of the elements in this class
    public Character getCharacter() {
        return this.character;
    }
    public void setCharacter(Character val) {
        this.character = val;
    }

    public Colour getForegroundColour() {
        return this.foregroundColour;
    }
    public void setForegroundColour(Colour val) {
        this.foregroundColour = val;
    }

    public Colour getBackgroundColour() {
        return this.backgroundColour;
    }
    public void setBackgroundColour(Colour val) {
        this.backgroundColour = val;
    }

    public boolean getStyleFlag(Style flag) {
        return this.styleFlags[flag.ordinal()];
    }
    public void setStyleFlag(Style flag, boolean val) {
        this.styleFlags[flag.ordinal()] = val;
    }

    public TrailFlag getTrailFlag() {
        return this.trailFlag;
    }
    public void setTrailFlag(TrailFlag val) {
        this.trailFlag = val;
    }

    public boolean[] getAllStyleFlags() {
        return this.styleFlags;
    }
    public void setAllStyleFlags(boolean vals[]) {
        this.styleFlags = vals;
    }

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

    public void copy(CharacterCell c) {
        this.character = c.getCharacter();
        this.backgroundColour = c.getBackgroundColour();
        this.foregroundColour = c.getForegroundColour();
        this.styleFlags = c.getAllStyleFlags();
        this.trailFlag = c.getTrailFlag();
    }
}
