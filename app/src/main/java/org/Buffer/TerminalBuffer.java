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
     * @param scrollMax the maximum number of lines the scrollback buffers can store. If the input is negative, it will be assumed to be {@code Integer.MAX_VALUE}
     * @param backgroundColour the background colour of the screen
     * @param foregroundColour the foreground colour of the screen
     */
    public TerminalBuffer(Integer width, Integer height, Integer scrollMax, Colour backgroundColour, Colour foregroundColour) {
        this.width = 0;
        this.height = 0;
        this.scrollMaximum = scrollMax > 0 ? scrollMax : Integer.MAX_VALUE;
        this.screenBackgroundColour = backgroundColour;
        this.screenForegroundColour = foregroundColour;

        this.cursorX = 0;
        this.cursorY = 0;

        lines = new ArrayList<>(height * 2);
        for(int i = 0; i < lines.size(); i++) {
            lines.add(new TerminalLine(width));
        }
    }

    /**
     * Constructor for a TerminalBuffer not including paramters for screen colours. This will default the background and foreground colours to black and white respectively
     * @param width the width of the screen represented by the buffer in characters
     * @param height the height of the screen represented by the buffer in lines
     * @param scrollMax the maximum number of lines the scrollback buffers can store. If the input is negative, it will be assumed to be {@code Long.MAX_VALUE}
     */
    public TerminalBuffer(Integer width, Integer height, Integer scrollMax) {
        this(width, height, scrollMax, Colour.BLACK, Colour.WHITE);
    }

    //Getters for CursorX and Y
    public Integer getCursorX() {
        return this.cursorX;
    }
    public Integer getCursorY() {
        return this.cursorY;
    }

    /**
     * Sets a cursor's x position to the specified position, clamped between [0, line_width), where line_width is the number of characters in the unwrapped line the cursor is on
     * @param val the new x position to move the cursor to
     */
    public void setCursorX(Integer val) {
        //Need to clamp the cursor's X position between [0, width)
        if (val <= 0) this.cursorX = 0;
        else if(val >= this.lines.get(cursorY).size()) this.cursorX = this.lines.get(cursorY).size() - 1;
        else this.cursorX = val;
    }

    /**
     * Sets the cursor's y position clamped between [0, number of lines)
     * @param val the new y position to move the cursor to
     */
    public void setCursorY(Integer val) {
        if(val < 0) this.cursorY = 0;
        else if(val >= lines.size()) this.cursorY = lines.size() - 1;
        else this.cursorY = val;
    }

    /**
     * Moves the cursor's x position by some amount of steps. Negative values move to the left, positive values move to the right.
     * The cursor's end position is clamped between [0, line_width), where line_width is the number of characters in the unwrapped line the cursor is on
     * @param val the number of steps to move
     */
    public void moveCursorX(Integer val) {
        setCursorX(this.cursorX + val);
    }
    /**
     * Moves the cursor's y position by some amount of steps. Negative values move up, positive values move down.
     * The cursor's end position is clamped between [0, number of lines)
     * @param val the number of steps to move
     */
    public void moveCursorY(Integer val) {
        setCursorY(this.cursorY + val);
    }

    /**
     * Overwrites a character with the specified text and moves the cursor one character to the right, stopping when it hits the end of the logical line
     * @param text the new character to write on the screen
     */
    public void writeText(Character text) {
        CharacterCell currCell = lines.get(cursorY).get(cursorX);
        currCell.setCharacter(text);

        //Stop when you hit the logical line
        if(cursorX < this.lines.get(cursorY).size())
            moveCursorX(1);
    }

    /**
     * Inserts text into a line
     * @param text the new character to be added
     */
    public void insertText(Character text) {
        lines.get(cursorY).add(new CharacterCell(text, Colour.DEFAULT, Colour.DEFAULT, Style.NONE));
    }

    /**
     * Adds a new line to the terminal buffer. If scrollMaximum is enabled, and
     * the current number of logical lines in the buffer is greater than scrollMaximum, remove the top line before adding a new one
     */
    public void addLine() {
        if(lines.size() >= scrollMaximum) {
            lines.remove(0);
            cursorY--;
        }
        lines.add(new TerminalLine(this.width));
        cursorY++;
    }

    /**
     * Clears the entire line buffer
     */
    public void clearAllLines() {
        for(int i = 0; i < lines.size(); i++) {
            lines.get(i).clear();
        }
    }
}
