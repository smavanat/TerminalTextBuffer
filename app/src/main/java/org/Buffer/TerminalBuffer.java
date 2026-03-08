package org.Buffer;

import java.util.ArrayList;

public class TerminalBuffer {
    private ArrayList<TerminalLine> lines; //A list of logical lines present in this terminal buffer
    private Integer width; //Width of the screen represented by the buffer in characters
    private Integer height; //Height of the screen represented by the buffer in lines
    private Integer scrollMaximum; //Maximum number of characters that can be held in the scrollback or scrollforward
    private Colour screenBackgroundColour; //The background colour of the screen. Defaults to black
    private Colour screenForegroundColour; //The foreground colour of the screen. Defaults to white
    private Integer cursorX; //X position of the cursor. The x-axis moves from left to right
    private Integer cursorY; //Y position of the cursor. The y-axis moves from top to bottom

    /**
     * Constructor for a TerminalBuffer including paramters for screen colours
     * @param width the width of the screen represented by the buffer in characters
     * @param height the height of the screen represented by the buffer in lines
     * @param scrollMax the maximum number of lines the scrollback and scrollforward buffers can each store. If the input is negative, it will be assumed to be {@code Integer.MAX_VALUE}
     * @param backgroundColour the background colour of the screen
     * @param foregroundColour the foreground colour of the screen
     */
    public TerminalBuffer(Integer width, Integer height, Integer scrollMax, Colour backgroundColour, Colour foregroundColour) {
        lines = new ArrayList<>(height * 2);
        for(int i = 0; i < lines.size(); i++) {
            lines.add(new TerminalLine(width));
        }
    }

    /**
     * Constructor for a TerminalBuffer not including paramters for screen colours. This will default the background and foreground colours to black and white respectively
     * @param width the width of the screen represented by the buffer in characters
     * @param height the height of the screen represented by the buffer in lines
     * @param scrollMax the maximum number of lines the scrollback and scrollforward buffers can each store. If the input is negative, it will be assumed to be {@code Long.MAX_VALUE}
     */
    public TerminalBuffer(Integer width, Integer height, Integer scrollMax) {
        this(width, height, scrollMax, Colour.BLACK, Colour.WHITE);
    }

    public Integer getCursorX() {
        return this.cursorX;
    }
    public Integer getCursorY() {
        return this.cursorY;
    }

    public void setCursorX(Integer val) {
        //Need to clamp the cursor's X position between [0, width)
        if (val <= 0) this.cursorX = 0;
        else if(val >= this.width) this.cursorX = width - 1;
        else this.cursorX = val;
    }

    //If the Y-cursor is negative, that means we have gone into the scrollback
    //If the Y cursor >= height, that means we have gone into the scrollforward
    //In both of these cases, we need to keep Y in the range [0, height), and simply adjust the contents of the scrollback, screen and scrollforward to compensate

    /**
     * Sets the cursor y value to the coordinate specified. If the value is negative, this is assumed to be a value in the scrollback
     * If the value is >= the screen height, this is assumed to be in the scrollforward.
     * The function will clamp the y position of the cursor to the number of lines stored in the scrollback/forward.
     * The function will also shift the screen if necessary to show the new content at this new Y position
     * @param val the new y coordinate to move to
     */
    public void setCursorY(Integer val) {
        if(val < 0) this.cursorY = 0;
        else if(val >= lines.size()) this.cursorY = lines.size() - 1;
        else this.cursorY = val;
    }

    public void moveCursorX(Integer val) {
        setCursorX(this.cursorX + val);
    }
    public void moveCursorY(Integer val) {
        setCursorY(this.cursorY + val);
    }

    /**
     * Overwrites a character with the specified text and moves the cursor one character to the right, moving onto a new line when it hits the edge
     * @param text the new character to write on the screen
     */
    public void writeText(Character text) {
        CharacterCell currCell = lines.get(cursorY).get(cursorX);
        currCell.setCharacter(text);

        moveCursorX(1);

        //Move to a new line when you hit the edge
        if(cursorX >= this.width) {
            moveCursorY(1);
            setCursorX(0);
        }
    }

    public void insertText(Character text) {
        lines.get(cursorY).add(new CharacterCell(text, Colour.DEFAULT, Colour.DEFAULT, Style.NONE));
    }
}
