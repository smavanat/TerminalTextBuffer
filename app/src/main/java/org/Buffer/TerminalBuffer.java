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
     * CONSTRUCTORS
     */

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
        this.scrollMaximum = scrollMax >= 0 ? scrollMax : Integer.MAX_VALUE;
        this.screenBackgroundColour = backgroundColour;
        this.screenForegroundColour = foregroundColour;

        this.cursorX = 0;
        this.cursorY = 0;
        this.bottomIndex = 0;

        this.scrollback = new ArrayList<>(height * 2);
        this.scrollback.add(new ArrayList<CharacterCell>(this.width));
        this.screen = new CircularArray<>(height);
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

    /**
     * GETTERS AND SETTERS
     */

    /**
     * @return the on-screen x position of the cursor
     */
    public Integer getScreenCursorX() {
        return this.cursorX % this.width;
    }

    /**
     * @return the on-screen y position of the cursor
     */
    public Integer getScreenCursorY() {
        int screenY = this.screen.size()-1; //Need it to be number of actual lines in the screen rather than height to avoid errors when not enough lines to fill the whole screen
        int logicalY = bottomIndex;

        //See how far up in the screen the cursor's logical line is
        while(logicalY >= 0 && logicalY != cursorY) {
            screenY -= logicalToTerminal(logicalY);
            logicalY--;
        }
        //See how many extra lines the characters after the logical x-position of the cursor are
        screenY -= ((scrollback.get(cursorY).size() - cursorX - 1 + this.width-1)/this.width);
        return Math.max(0, Math.min(screenY, height - 1)); //Clamp the value
    }

    public Integer getScrollMaximum() {
        return this.scrollMaximum;
    }

    public void setScrollMaximum(Integer val) {
        this.scrollMaximum = val >= 0 ? val : Integer.MAX_VALUE;
    }

    //Getters for logical cursor X and Y
    public Integer getCursorX() {
        return this.cursorX;
    }

    public Integer getCursorY() {
        return this.cursorY;
    }

    public Colour getScreenBackgroundColour() {
        return this.screenBackgroundColour;
    }

    public Colour getScreenForegroundColour() {
        return this.screenForegroundColour;
    }

    public void setScreenBackgroundColour(Colour val) {
        if(val == Colour.DEFAULT) return; //Avoid breaking the colour system
        this.screenBackgroundColour = val;
    }

    public void setScreenForegroundColour(Colour val) {
        if(val == Colour.DEFAULT) return; //Avoid breaking the colour system
        this.screenForegroundColour = val;
    }

    public Integer getHeight() {
        return this.height;
    }

    public Integer getWidth() {
        return this.width;
    }

    public void setHeight(Integer val) {
        this.height = val;
        screen.clear();
        rebuildScreen();
    }

    public void setWidth(Integer val) {
        this.width = val;
        screen.clear();
        rebuildScreen();
    }

    public TerminalLine getScreenLine(int index) {
        return screen.get(index);
    }

    public int getScreenSize() {
        return screen.size();
    }

    /**
     * CURSOR OPERATIONS
     */

    /**
     * Sets a cursor's x position to the specified position, clamped between [0, logical line width)
     * If the cursor ends at a cell with {@link TrailFlag} WIDE_END, move one cell to the left
     * @param val the new x position to move the cursor to
     */
    public void setCursorX(Integer val) {
        cursorX = Math.max(0, Math.min(val, scrollback.get(cursorY).size()));

        if(cursorX > 0 && cursorX < scrollback.get(cursorY).size() && scrollback.get(cursorY).get(cursorX).getTrailFlag() == TrailFlag.WIDE_END) //Cursor can never land on the end of a wide character
            cursorX -= 1;
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
        setCursorX(Math.min(cursorX, scrollback.get(cursorY).size()));
    }

    /**
     * Moves the cursor's x position by some amount of steps. Negative values move to the left, positive values move to the right.
     * The cursor's end position is clamped between [0, line_width), where line_width is the number of characters in the unwrapped line the cursor is on
     * If the cursor ends at a cell with {@link TrailFlag} WIDE_END, move one cell in the direction you want to move to
     * @param val the number of steps to move */
    public void moveCursorX(Integer val) {
        int pos = cursorX + val;

        ArrayList<CharacterCell> line = scrollback.get(cursorY);
        //If we are moving to an empty flag, skip it
        if(pos > 0 && pos < line.size() && line.get(pos).getTrailFlag() == TrailFlag.WIDE_END) {
            if (pos < line.size() - 1 && val > 0) pos += 1; //If moving right
            else if (pos > 0 && val < 0) pos -= 1; //If moving left
        }
        setCursorX(pos);
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
     * BUFFER MANIPULATION
     */

    /**
     * Moves the bottom index of the screen by some number of spaces
     * Clears the current screen buffer and rebuilds it using the new bottom of the screen as a reference into the scrollback
     * @param spaces the number of spaces to scroll by. negative means down, positive means up
     */
    public void scroll(Integer spaces) {
        bottomIndex = Math.max(0, Math.min(bottomIndex + spaces, scrollback.size() - 1)); //Calculate the new screen bottom

        rebuildScreen();
    }

    /**
     * Adds an empty line to the bottom of the screen and removes extra lines if we are over the scrollback buffer.
     * Moves the cursor down one line, scrolling if necessary
     */
    public boolean createNewLine() {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1) return false; //Early exit when not at the bottom of the screen

        addNewLine();
        rebuildScreen();
        return true;
    }

    /**
     * Clears the lines from the screen. Does not remove anything from the scrollback
     */
    public void clearScreen() {
        screen.clear();
        addNewLine();
        screen.addToFront(new TerminalLine(width));
    }

    /**
     * Clears all data in screen and scrollback buffers and resets the cursor position
     */
    public void clearEntireBuffer() {
        //Clearing buffers and setting them to only have one blank element
        screen.clear();
        scrollback.clear();
        screen.addToFront(new TerminalLine(this.width));
        scrollback.add(new ArrayList<CharacterCell>(this.width));

        //Resetting cursor position
        cursorX = 0;
        cursorY = 0;
    }

    /**
     * TEXT EDITING -> The user will only be able to edit text if it is on the bottom line of the screen
     */

    /**
     * Inserts text at the mouse cursor's position only if the cursor is at the bottom of the screen and scrollback
     * Moves the cursor to the right w times, where w is the width of the input {@link Character}
     * If the cursor starts at the end of a wide character, moves one space to the left before writing
     * If inserting the input at the current position would overlap with other wide characters, erase them first
     * @param text the new character to add
     * @return true if the text was inserted, false if the cursor is not at the bottom line
     */
    public boolean insertText(Character text) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1) return false;

        int oldLines = logicalToTerminal(cursorY);

        int charWidth = getCharWidth(text);
        if(charWidth < 1) return false; // skip 0-width or invalid

        ArrayList<CharacterCell> line = scrollback.get(cursorY);
        // Move cursor back if on the second half of a wide char
        if(cursorX > 0 && cursorX < line.size() && line.get(cursorX).getTrailFlag() == TrailFlag.WIDE_END) {
            cursorX -= 1;
        }

        clearOverlappingWideChars(charWidth);

        if(charWidth == 1) {
            line.add(cursorX, new CharacterCell(text));
        } else { // width == 2
            line.add(cursorX, new CharacterCell(text, TrailFlag.WIDE_START));
            line.add(cursorX + 1, new CharacterCell(text, TrailFlag.WIDE_END));
        }

        if(oldLines < logicalToTerminal(cursorY)) rebuildScreen(); //Need to shift the screen down
        else {
            System.out.println("Screen Cursor X: " + getScreenCursorX());
            if(charWidth == 1) {
                screen.get(getScreenCursorY()).add(getScreenCursorX(), new CharacterCell(text));
            } else { // width == 2
                screen.get(getScreenCursorY()).add(getScreenCursorX(), new CharacterCell(text, TrailFlag.WIDE_START));
                screen.get(getScreenCursorY()).add(getScreenCursorX() + 1, new CharacterCell(text, TrailFlag.WIDE_END));
            }
        }

        cursorX += charWidth;

        return true;
    }

    /**
     * Overwrites text at the mouse cursor's current position only if the cursor is at the bottom of the screen and scrollback.
     * If the new character and current character have the same width, just replaces the character
     * If the new character has a width of 1 and the old character has a width of 2, removes the trailing character
     * If the new character has a width of 2 and the old character has a width of 1, adds a trailing character
     * Moves the mouse <newCharWidth> spaces to the right, stopping if it has reached the end of the existing text
     * @param text the new character to add
     * @return true if the text was overwritten, false if the cursor is not at the bottom line
     */
    public boolean overwriteText(Character text) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1 || cursorX >= scrollback.get(cursorY).size()) return false; //Early exit when not at the bottom of the screen

        ArrayList<CharacterCell> line = scrollback.get(cursorY);
        int oldLines = logicalToTerminal(cursorY);

        int oldCharWidth = getCharWidth(line.get(cursorX).getCharacter());
        int newCharWidth = getCharWidth(text);

        if(oldCharWidth == 2 && cursorX > 0 && line.get(cursorX).getTrailFlag() == TrailFlag.WIDE_END) {
            cursorX -= 1;
        }

        line.get(cursorX).setCharacter(text);
        screen.get(getScreenCursorY()).get(getScreenCursorX()).setCharacter(text);

        if(oldCharWidth == 1 && newCharWidth == 2)
            line.add(cursorX+1, new CharacterCell(null, TrailFlag.WIDE_END));
        if(oldCharWidth == 2 && newCharWidth == 1)
            line.remove(cursorX+1);

        if(oldLines != logicalToTerminal(cursorY)) rebuildScreen(); //Need to shift the screen if adding different sized character changes number of terminal lines
        else {
            if(oldCharWidth == 1 && newCharWidth == 2)
                screen.get(getScreenCursorY()).add(getScreenCursorX()+1, new CharacterCell(null, TrailFlag.WIDE_END));
            if(oldCharWidth == 2 && newCharWidth == 1)
                screen.get(getScreenCursorY()).remove(getScreenCursorX()+1);
        }

        moveCursorX(newCharWidth == 2 ? 2 : 1);

        return true;
    }

    /**
     * Sets the background colour at the mouse cursor's current position only if the cursor is at the bottom of the screen and scrollback.
     * Does not move the mouse position
     * @param colour the new background colour of the cell
     * @return true if the text was overwritten, false if the cursor is not at the bottom line
     */
    public boolean setBackgroundColourAtCursorPos(Colour colour) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1 || 
            scrollback.get(cursorY).size() == cursorX) return false; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        scrollback.get(cursorY).get(cursorX).setBackgroundColour(colour); //Overwriting the colour in this position
        return true;
    }

    /**
     * Sets the foreground colour at the mouse cursor's current position only if the cursor is at the bottom of the screen and scrollback.
     * Does not move the mouse position
     * @param colour the new foreground colour of the cell
     * @return true if the text was overwritten, false if the cursor is not at the bottom line
     */
    public boolean setForegroundColourAtCursorPos(Colour colour) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1 || 
            scrollback.get(cursorY).size() == cursorX) return false; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        scrollback.get(cursorY).get(cursorX).setForegroundColour(colour); //Overwriting the colour in this position
        return true;
    }

    /**
     * Sets the style at the mouse cursor's current position only if the cursor is at the bottom of the screen and scrollback.
     * Does not move the mouse position
     * @param style the new style of the cell
     * @return true if the text was overwritten, false if the cursor is not at the bottom line
     */
    public boolean setStyleAtCursorPos(Style style) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1 || 
            scrollback.get(cursorY).size() == cursorX) return false; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        scrollback.get(cursorY).get(cursorX).setStyleFlag(style);//Overwriting the style in this position
        return true;
    }

    /**
     * Clears the last line in the buffer and rebuilds the screen if necessary. Only works if the cursor is at the bottom line
     * @return true if the line was cleared, false if the cursor is not at the bottom line
     */
    public boolean clearLine() {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1) return false; //Early exit when not at the bottom of the screen

        scrollback.get(cursorY).clear();
        rebuildScreen(); //Remake the layout
        return true;
    }

    /**
     * Fills a terminal line with the specified character starting from the cursor's current screen position
     * e.g. if the screen's cursor position is 1 and the width is 4, 3 cells will be filled
     * Exits early if the cursor is not at the bottom line
     * Moves a cursor along with the fill
     * Since the input is a {@link CharacterCell}, the colours and style of the character can be specified
     * @return true if the line was cleared, false if the cursor is not at the bottom line
     */
    public boolean fillLineWithChar(CharacterCell fill) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1) return false; //Early exit when not at the bottom of the screen

        for(int i = getScreenCursorX(); i < width; i++) {
            scrollback.get(cursorY).add(fill);
            cursorX++; //Moving the cursor
        }
        rebuildScreen();

        return true;
    }

    /**
     * Fills a terminal line with the specified character starting from the cursor's current screen position
     * e.g. if the screen's cursor position is 1 and the width is 4, 3 cells will be filled
     * Exits early if the cursor is not at the bottom line
     * Moves a cursor along with the fill
     * Since the input is a {@link Character}, the colours and style of the character will be set to the TerminalBuffer's defaults
     * @return true if the line was cleared, false if the cursor is not at the bottom line
     */
    public boolean fillLineWithChar(Character fill) {
        if(cursorY != scrollback.size()-1 || bottomIndex != scrollback.size()-1) return false; //Early exit when not at the bottom of the screen

        CharacterCell defaultFill = new CharacterCell(fill);
        for(int i = getScreenCursorX(); i < width; i++) {
            scrollback.get(cursorY).add(defaultFill);
            cursorX++; //Moving the cursor
        }
        rebuildScreen();

        return true;
    }

    /**
     * RETRIEVING BUFFER CONTENT
     */

    /**
     * @return the character stored in the cell at the current cursor's position if the cursor is not beyond the end of the line
     */
    public Character getCharAtCursorPos() {
        if(scrollback.get(cursorY).size() == cursorX) return null; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        return scrollback.get(cursorY).get(cursorX).getCharacter();
    }

    /**
     * @return the background colour stored in the cell at the current cursor's position if the cursor is not beyond the end of the line
     */
    public Colour getBackgroundColourAtCursorPos() {
        if(scrollback.get(cursorY).size() == cursorX) return Colour.DEFAULT; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        return scrollback.get(cursorY).get(cursorX).getBackgroundColour() == Colour.DEFAULT ? this.screenBackgroundColour : scrollback.get(cursorY).get(cursorX).getBackgroundColour();
    }

    /**
     * @return the foreground colour stored in the cell at the current cursor's position if the cursor is not beyond the end of the line
     */
    public Colour getForegroundColourAtCursorPos() {
        if(scrollback.get(cursorY).size() == cursorX) return Colour.DEFAULT; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        return scrollback.get(cursorY).get(cursorX).getForegroundColour() == Colour.DEFAULT ? this.screenForegroundColour : scrollback.get(cursorY).get(cursorX).getForegroundColour();
    }

    /**
     * @return the style stored in the cell at the current cursor's position if the cursor is not beyond the end of the line
     */
    public Style getStyleAtCursorPos() {
        if(scrollback.get(cursorY).size() == cursorX) return Style.NONE; //Early exit when not at the bottom of the screen and the cursor is on the end of the line

        return scrollback.get(cursorY).get(cursorX).getStyleFlag();
    }

    /**
     * @return the entire terminal line the cursor is currently on as a {@link String}
     */
    public String printScreenLine() {
        StringBuilder buf = new StringBuilder();
        TerminalLine line = screen.get(getScreenCursorY());

        for(int i = 0; i < line.size(); i++) {
            buf.append(line.get(i).getCharacter());
        }
        buf.append('\n');

        return buf.toString();
    }


    /**
     * @return the entire logical line the cursor is currently on as a {@link String}
     */
    public String printScrollLine() {
        StringBuilder buf = new StringBuilder();
        ArrayList<CharacterCell> line = scrollback.get(cursorY);

        for(int i = 0; i < line.size(); i++) {
            buf.append(line.get(i).getCharacter());
        }
        buf.append('\n');

        return buf.toString();
    }

    /**
     * @return entire screen content as a string. Does not handle colours or styles
     */
    public String printScreenContents() {
        StringBuilder buf = new StringBuilder();

        for(int i = 0; i < screen.size(); i++) {
            for(int j = 0; j < screen.get(i).size(); j++) {
                buf.append(screen.get(i).get(j).getCharacter());
            }
            buf.append("\n")    ;
        }

        return buf.toString();
    }

    /**
     * @return the entire scrollback content as a string. Does not handle colours or styles
     */
    public String printScrollbackContents() {
        StringBuilder buf = new StringBuilder();

        for(int i = 0; i < scrollback.size(); i++) {
            for(int j = 0; j < scrollback.get(i).size(); j++) {
                buf.append(scrollback.get(i).get(j).getCharacter());
            }
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     * HELPER FUNCTIONS
     */

    /**
     * Rebuilds the screen from the bottom index
     */
    private void rebuildScreen() {
        screen.clear(); //Scroll down

        int start = Math.max(0, bottomIndex - height + 1); //Get the top of the new screen

        for(int i = start; i <= bottomIndex; i++) {
            wrapLogicalLine(scrollback.get(i)); //Wrap the line so it fits the current screen width
        }
    }

    /**
     * Wraps logical lines so they fit the screen width
     * If the character at the end of a screen line would be the first half of a 2-wide character,
     * wraps onto a new line
     * @param logical the full logical line
     */
    private void wrapLogicalLine(ArrayList<CharacterCell> logical) {
        int index = 0;

        while(index < logical.size()) {
            TerminalLine screenLine = new TerminalLine(this.width, index != 0); //Creating a new screen line

            int x = 0; //Tracks the x position in the screen line
            while(x < width && index < logical.size()) {
                CharacterCell cell = logical.get(index);
                int charWidth = (cell.getTrailFlag() == TrailFlag.WIDE_START) ? 2 : 1;

                if(x + charWidth > width) break; //Wide characters at the end of a line must go onto a newline

                screenLine.add(cell);
                x += charWidth;
                index++;
            }

            screen.addToBack(screenLine); //Adding it to the bottom of the screen

            //Removing excess screen lines
            if(screen.size() > height) {
                screen.removeFromFront();
            }
        }
    }

    /**
     * Adds an empty line to the bottom of the screen and removes extra lines if we are over the scrollback buffer
     * Does not move the screen contents
     */
    private void addNewLine() {
        scrollback.add(new ArrayList<CharacterCell>(this.width));
        if(scrollback.size() > Math.max(scrollMaximum + this.height, scrollMaximum)) { //The scrollback can only hold scrollMaximum number of extra lines past the lines in the screen
            scrollback.remove(0);
            bottomIndex = Math.max(0, bottomIndex - 1);
            cursorY = Math.max(0, cursorY - 1);
        }
        cursorY++;
        cursorX = 0;
        if (cursorY > bottomIndex) {
            bottomIndex = cursorY;
        }
    }

    /**
     * Helper function to get the logical line at the top of the current screen
     * @return the index of the logical line at the top of the screen
     */
    private int getLogicalScreenTop() {
        int screenTop = bottomIndex; //The logical line at the top of the screen
        int remainingRows = this.height; //Number of rows we haven't seen to be filled by a logical line in the screen

        while(screenTop > 0 && remainingRows > 0) {
            int lineRows = logicalToTerminal(screenTop-1);
            if(lineRows > remainingRows) break; //If the number of lines the current screen top takes up is more than the remaining unfilled rows on the screen, break
            remainingRows -= lineRows; //Otherwise decrease the number of remaining unfilled rows on the screen
            screenTop--; //Move to the next line up
        }

        return screenTop;
    }

    /**
     * Helper function to see how many Terminal screen lines the logical line at the given index takes up
     * @param index the index of the logical line whose size in terminal lines we want to know
     * @return the number of lines this logical line takes up
     */
    private int logicalToTerminal(int index) {
        return (scrollback.get(index).size() + this.width-1) /this.width;
    }

    /**
     * Returns the width of a character
     * @param charVal the unicode value of the character
     * @return -1 for control characters, 0 for combining marks, 2 for wide characters, and 1 otherwise
     */
    private int getCharWidth(int charVal) {
        //Control characters
        if (charVal == 0)
            return 0;
        if (charVal < 32 || (charVal >= 0x7f && charVal < 0xa0))
            return -1;

        //Combining marks
        int type = Character.getType(charVal);
        if(type == Character.NON_SPACING_MARK || type == Character.COMBINING_SPACING_MARK || type == Character.ENCLOSING_MARK) return 0;

        //Wide characters
        if(isWide(charVal)) return 2;

        return 1; //Default
    }

    /**
     * Function to determine if a character is two spaces wide
     * Uses the Marcus Khun wcwidth tables: https://www.cl.cam.ac.uk/~mgk25/ucs/wcwidth.c
     * @param charVal the unicode value of the char
     * @return true if it is two characters wide, false otherwise
     */
    private boolean isWide(int charVal) {
        return
            charVal >= 0x1100 && (
                charVal <= 0x115F ||                 // Hangul Jamo
                charVal == 0x2329 || charVal == 0x232A ||
                (charVal >= 0x2E80 && charVal <= 0xA4CF && charVal != 0x303F) ||
                (charVal >= 0xAC00 && charVal <= 0xD7A3) || // Hangul syllables
                (charVal >= 0xF900 && charVal <= 0xFAFF) || // CJK compatibility
                (charVal >= 0xFE10 && charVal <= 0xFE19) ||
                (charVal >= 0xFE30 && charVal <= 0xFE6F) ||
                (charVal >= 0xFF00 && charVal <= 0xFF60) ||
                (charVal >= 0xFFE0 && charVal <= 0xFFE6) ||
                (charVal >= 0x20000 && charVal <= 0x3FFFD)
        );
    }

    private void clearOverlappingWideChars(int charWidth) {
        if(charWidth != 1 && charWidth != 2) return; // Only handle width 1 and 2

        ArrayList<CharacterCell> line = scrollback.get(cursorY);

        //Ensure cursorX is within bounds
        if(cursorX < 0) cursorX = 0;
        if(cursorX >= line.size()) return;

        CharacterCell currentCell = line.get(cursorX);

        //If cursor is over the second half of a wide char, move to start
        if(currentCell.getTrailFlag() == TrailFlag.WIDE_END) {
            cursorX -= 1;
            currentCell = line.get(cursorX);
        }

        //If current cell is part of a wide char, clear both cells
        if(currentCell.getTrailFlag() == TrailFlag.WIDE_START) {
            //Clear first cell
            currentCell.setCharacter(' ');
            currentCell.setTrailFlag(TrailFlag.NORMAL);

            //Clear second cell (make sure it exists)
            if(cursorX + 1 < line.size()) {
                CharacterCell nextCell = line.get(cursorX + 1);
                if(nextCell.getTrailFlag() == TrailFlag.WIDE_END) {
                    nextCell.setCharacter(' ');
                    nextCell.setTrailFlag(TrailFlag.NORMAL);
                } else {
                    // This should never happen in a consistent state
                    throw new IllegalStateException("Wide char WIDE_START not followed by WIDE_END");
                }
            }
        }

        //For width-2 new character, also check if next cell is the start of a wide char
        if(charWidth == 2 && cursorX + 1 < line.size()) {
            CharacterCell nextCell = line.get(cursorX + 1);
            if(nextCell.getTrailFlag() == TrailFlag.WIDE_START) {
                // Clear both cells of the next wide char
                nextCell.setCharacter(' ');
                nextCell.setTrailFlag(TrailFlag.NORMAL);

                if(cursorX + 2 < line.size()) {
                    CharacterCell nextNextCell = line.get(cursorX + 2);
                    if(nextNextCell.getTrailFlag() == TrailFlag.WIDE_END) {
                        nextNextCell.setCharacter(' ');
                        nextNextCell.setTrailFlag(TrailFlag.NORMAL);
                    } else {
                        throw new IllegalStateException("Wide char WIDE_START not followed by WIDE_END");
                    }
                }
            }
        }
    }
}
