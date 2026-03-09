package org.Buffer;

import java.util.ArrayList;

public class TerminalBuffer {
    private ArrayList<ArrayList<CharacterCell>> scrollback; //A list of all logical lines present in this terminal buffer
    private CircularArray<TerminalLine> screen; //A list of all the lines in the screen
    private Integer width; //Width of the screen represented by the buffer in characters
    private Integer height; //Height of the screen represented by the buffer in lines
    private Integer scrollMaximum; //Maximum number of characters that can be held in the scrollback or scrollforward
    private Colour screenBackgroundColour; //The background colour of the screen. Defaults to black
    private Colour screenForegroundColour; //The foreground colour of the screen. Defaults to white
    private Integer cursorX; //X position of the cursor. The x-axis moves from left to right
    private Integer cursorY; //Y position of the cursor. The y-axis moves from top to bottom
    private Integer bottomIndex; //Internal line pointer into the scollback buffer

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
        this.bottomIndex = 0;

        this.scrollback = new ArrayList<>(height * 2);
        this.scrollback.add(new ArrayList<CharacterCell>(this.width));
        this.screen = new CircularArray<>();
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

    //Getters for cursor X and Y
    public Integer getCursorX() {
        return this.cursorX;
    }

    public Integer getCursorY() {
        return this.cursorY;
    }

    /**
     * Sets a cursor's x position to the specified position, clamped between [0, width)
     * @param val the new x position to move the cursor to
     */
    public void setCursorX(Integer val) {
        cursorX = Math.max(0, Math.min(val, width-1));
    }

    /**
     * Sets a cursor's y position to the specified position, clamped between [0, height)
     * @param val the new x position to move the cursor to
     */
    public void setCursorY(Integer val) {
        cursorY = Math.max(0, Math.min(val, height-1));
    }

    /**
     * Moves the cursor's x position by some amount of steps. Negative values move to the left, positive values move to the right.
     * The cursor's end position is clamped between [0, line_width), where line_width is the number of characters in the unwrapped line the cursor is on
     * @param val the number of steps to move
     */
    public void moveCursorX(Integer val) {
        //If we would move beyond the limits of the current line, check if the previous line is wrapped, and if it is, move to its end
        if(cursorX + val < 0 && screen.get(cursorY).getWrapped()) {
            //Move cursor to end of previous line
            moveCursorY(-1);
            setCursorX(this.width-1);
        }
        //If we would move beyond the limits of the current line, check if the next line is wrapped, and if it is, move to its start
        else if(cursorX + val > screen.get(cursorY).size() && screen.get(cursorY+1).getWrapped()) {
            //Move cursor to start of next line
            setCursorX(0);
            moveCursorY(1);
        }
        //Otherwise just move the cursor
        else {
            setCursorX(cursorX + val);
        }
    }
    /**
     * Moves the cursor's y position by some amount of steps. If the movement would cause the cursor to move off the screen, scroll
     * The cursor's end position is clamped between [0, number of lines)
     * @param val the number of steps to move
     */
    public void moveCursorY(Integer val) {
        //If the new position is below the current screen bottom, scroll down
        if(cursorY + val >= this.height){
            scroll(val - this.height);
            setCursorY(0); //Set the cursor to the top of the screen
        }
        //If the new position is above the current height, scroll up
        else if (cursorY + val < 0) {
            scroll(cursorY + val);
            setCursorY(this.height - 1); //Set the cursor to the bottom of the screen
        }
    }

    /**
     * Moves the bottom index of the screen by some number of spaces
     * Clears the current screen buffer and rebuilds it using the new bottom of the screen as a reference into the scrollback
     * @param spaces the number of spaces to scroll by. negative means down, positive means up
     */
    public void scroll(Integer spaces) {
        bottomIndex = Math.max(0, Math.min(bottomIndex + spaces, scrollback.size() - 1)); //Calculate the new screen bottom

        screen.clear(); //Scroll down

        int start = Math.max(0, bottomIndex - height + 1); //Get the top of the new screen

        for(int i = start; i <= bottomIndex; i++) {
            wrapLogicalLine(scrollback.get(i)); //Wrap the line so it fits the current screen width
        }
    }

    /**
     * Wraps logical lines so they fit the screen width
     * @param logical the full logical line
     */
    private void wrapLogicalLine(ArrayList<CharacterCell> logical) {
        int index = 0;

        while(index < logical.size()) {
            TerminalLine screenLine = new TerminalLine(this.width, !(index < this.width)); //Creating a new screen line

            for(int x = 0; x < width && index < logical.size(); x++) { //Copying the character cells over
                screenLine.add(logical.get(index++));
            }

            screen.addToBack(screenLine); //Adding it to the bottom of the screen

            //Removing excess screen lines
            if(screen.size() > height) {
                screen.removeFromFront();
            }
        }
    }

    /**
     * Adds an empty line to the bottom of the screen and scrolls down
     */
    public void createNewLine() {
        scrollback.add(new ArrayList<CharacterCell>(this.width));
        scroll(1);
    }

    /**
     * Clears the lines from the screen. Does not remove anything from the scrollback
     */
    public void clearScreen() {
        screen.clear();
    }

    /**
     * Clears all data in screen and scrollback buffers
     */
    public void clearEntireBuffer() {
        screen.clear();
        scrollback.clear();
    }
}
