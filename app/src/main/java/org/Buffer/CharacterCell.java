package org.Buffer;

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
    private Style styleFlag;

    /**
     * Constructor for a CharacterCell
     * @param c the character to be stored in this cell
     * @param fc the foreground colour of the cell
     * @param bc the background colour of the cell
     * @param sf the style flag of the cell
     */
    public CharacterCell(Character c, Colour fc, Colour bc, Style sf) {
        this.character = c;
        this.foregroundColour= fc;
        this.backgroundColour = bc;
        this.styleFlag = sf;
    }

    /**
     * Constructor for CharacterCell where all other attributes are set to default values, i.e. DEFAULT, DEFAULT and NONE
     * @param c the character this CharacterCell should contain
     */
    public CharacterCell(Character c) {
        this(c, Colour.DEFAULT, Colour.DEFAULT, Style.NONE);
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

    public Style getStyleFlag() {
        return this.styleFlag;
    }

    public void setStyleFlag(Style val) {
        this.styleFlag = val;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;

        CharacterCell other = (CharacterCell) o;

        if(this.character.equals(other.getCharacter()) &&
            this.foregroundColour.equals(other.getForegroundColour()) &&
            this.backgroundColour.equals(other.getBackgroundColour()) &&
            this.styleFlag.equals(other.getStyleFlag())) return true;

        return false;
    }
}
