package org.Buffer;

import java.util.ArrayList;

/**
 * NOTE: Lines in the screen are set to have their wrapped attribute set to true if they wrap from the previous line,
 *       not if they wrap onto the next one
 */
public class TerminalBuffer {
    private ArrayList<ArrayList<CharacterCell>> scrollback; //A list of all logical lines present in this terminal buffer
    private CircularArray<TerminalLine> screen; //A list of all the lines in the screen
    private Integer width; //Width of the screen represented by the buffer in characters
    private Integer height; //Height of the screen represented by the buffer in lines
    private Integer scrollMaximum; //Maximum number of characters that can be held in the scrollback or scrollforward
    private Colour screenBackgroundColour; //The background colour of the screen. Defaults to black
    private Colour screenForegroundColour; //The foreground colour of the screen. Defaults to white
    private Integer cursorX; //Logical X position of the cursor. The x-axis moves from left to right.
    private Integer cursorY; //Logical Y position of the cursor. The y-axis moves from top to bottom
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
        this.width = width;
        this.height = height;
        this.scrollMaximum = scrollMax > 0 ? scrollMax : Integer.MAX_VALUE;
        this.screenBackgroundColour = backgroundColour;
        this.screenForegroundColour = foregroundColour;

        this.cursorX = 0;
        this.cursorY = 0;
        this.bottomIndex = 0;

        this.scrollback = new ArrayList<>(height * 2);
        this.scrollback.add(new ArrayList<CharacterCell>(this.width));
        this.screen = new CircularArray<>();
        screen.addToFront(new TerminalLine(this.width));
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

    public Integer getScreenCursorX() {
        return this.cursorX % this.width;
    }

    /**
     * Need to implement
     */
    public Integer getScreenCursorY() {
        return 0;
    }

    //Getters for cursor X and Y
    public Integer getCursorX() {
        return this.cursorX;
    }

    public Integer getCursorY() {
        return this.cursorY;
    }

    /**
     * Sets a cursor's x position to the specified position, clamped between [0, logical line width)
     * @param val the new x position to move the cursor to
     */
    public void setCursorX(Integer val) {
        cursorX = Math.max(0, Math.min(val, scrollback.get(cursorY).size()-1));
    }

    /**
     * Sets a cursor's y position to the specified position, clamped between [0, scrollback height)
     * @param val the new x position to move the cursor to
     */
    public void setCursorY(Integer val) {
        Integer screenTop = getLogicalScreenTop();
        Integer clampedVal = Math.max(0, Math.min(val, scrollback.size()-1));

        if(clampedVal < screenTop) {
            scroll(clampedVal - screenTop);
        }
        else if(clampedVal > bottomIndex) {
            scroll(clampedVal - bottomIndex);
        }

        cursorY = clampedVal;
    }

    /**
     * Helper function to get the logical line at the top of the current screen
     * @return the index of the logical line at the top of the screen
     */
    private int getLogicalScreenTop() {
        int screenTop = bottomIndex; //The logical line at the top of the screen
        int remainingRows = this.height; //Number of rows we haven't seen to be filled by a logical line in the screen

        while(screenTop > 0 && remainingRows > 0) {
            int lineRows = (scrollback.get(screenTop-1).size() + this.width-1)/width; //Get the cieling of the number of rows this line takes up
            if(lineRows > remainingRows) break; //If the number of lines the current screen top takes up is more than the remaining unfilled rows on the screen, break
            remainingRows -= lineRows; //Otherwise decrease the number of remaining unfilled rows on the screen
            screenTop--; //Move to the next line up
        }

        return screenTop;
    }

    /**
     * Moves the cursor's x position by some amount of steps. Negative values move to the left, positive values move to the right.
     * The cursor's end position is clamped between [0, line_width), where line_width is the number of characters in the unwrapped line the cursor is on
     * @param val the number of steps to move
     */
    public void moveCursorX(Integer val) {
        setCursorX(cursorX + val);
    }
    /**
     * Moves the cursor's y position by some amount of steps. If the movement would cause the cursor to move off the screen, scroll
     * The cursor's end position is clamped between [0, number of lines)
     * @param val the number of steps to move
     */
    public void moveCursorY(Integer val) {
        setCursorY(cursorY + val);
    }

    /**
     * Moves the bottom index of the screen by some number of spaces
     * Clears the current screen buffer and rebuilds it using the new bottom of the screen as a reference into the scrollback
     * @param spaces the number of spaces to scroll by. negative means down, positive means up
     */
    public void scroll(Integer spaces) {
        bottomIndex = Math.max(0, Math.min(bottomIndex + spaces, scrollback.size() - 1)); //Calculate the new screen bottom

        rebuildScreen();
    }

    private void rebuildScreen() {
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
            TerminalLine screenLine = new TerminalLine(this.width, index != 0); //Creating a new screen line

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
     * Adds an empty line to the bottom of the screen and moves the cursor down one line, scrolling if necessary
     */
    public void createNewLine() {
        scrollback.add(new ArrayList<CharacterCell>(this.width));
        if(scrollback.size() > scrollMaximum) {
            scrollback.remove(0);
            bottomIndex = Math.max(0, bottomIndex - 1);
            cursorY = Math.max(0, cursorY - 1);
        }
        cursorY++;
        bottomIndex = Math.min(scrollback.size() - 1, bottomIndex + 1);
        rebuildScreen();
    }

    /**
     * Inserts text at the mouse cursor's position, only if the cursor is at the bottom of the screen and scrollback
     * @param text the new character to add
     */
    public void insertText(Character text) {
        scrollback.get(cursorY).add(cursorX, new CharacterCell(text));
        moveCursorX(1);
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

    /**
     * Returns entire screen content as a string. Does not handle characters or styles
     */
    public String getScreenContents() {
        StringBuilder buf = new StringBuilder();

        for(int i = 0; i < screen.size(); i++) {
            for(int j = 0; j < screen.get(i).size(); j++) {
                buf.append(screen.get(i).get(j).getCharacter());
            }
            if(i < screen.size() - 1 && !screen.get(i+1).getWrapped()) buf.append('\n');
        }

        return buf.toString();
    }
}
