package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.Buffer.TerminalBuffer;
import org.Buffer.Colour;
import org.Buffer.Style;
import org.Buffer.CharacterCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TerminalBufferTest {
    private TerminalBuffer testBuffer;

    @BeforeEach
    void initialise() {
        testBuffer = new TerminalBuffer(2, 2, 2);
    }

    /**
     * ConstructorTests
     */

    /**
     * Test defaults
     */
    @Test
    void testConstructorDefaults() {
        assertEquals(2, testBuffer.getWidth());
        assertEquals(2, testBuffer.getHeight());
        assertEquals(2, testBuffer.getScrollMaximum());
        assertEquals(Colour.BLACK, testBuffer.getScreenBackgroundColour());
        assertEquals(Colour.WHITE, testBuffer.getScreenForegroundColour());
        assertEquals(0, testBuffer.getCursorX());
        assertEquals(0, testBuffer.getCursorY());
        assertEquals(0, testBuffer.getScreenCursorX());
        assertEquals(0, testBuffer.getScreenCursorY());
    }

    /**
     * Test Colour constructor
     */
    @Test
    void testConstructorWithColours() {
        TerminalBuffer buf = new TerminalBuffer(4, 2, 5, Colour.BLUE, Colour.YELLOW);
        assertEquals(Colour.BLUE, buf.getScreenBackgroundColour());
        assertEquals(Colour.YELLOW, buf.getScreenForegroundColour());
    }

    /**
     * Test giving a negative input for scrollMaximum
     */
    @Test
    void testConstructorScrollMaxNeg() {
        testBuffer = new TerminalBuffer(2, 2, -1);

        assertEquals(Integer.MAX_VALUE, testBuffer.getScrollMaximum());
    }

    /**
     * Getter and setter tests:
     */
    @Test
    void testColourSetters() {
        testBuffer.setScreenBackgroundColour(Colour.GREEN);
        testBuffer.setScreenForegroundColour(Colour.CYAN);

        assertEquals(Colour.GREEN, testBuffer.getScreenBackgroundColour());
        assertEquals(Colour.CYAN, testBuffer.getScreenForegroundColour());

        testBuffer.setScreenBackgroundColour(Colour.DEFAULT);
        testBuffer.setScreenForegroundColour(Colour.DEFAULT);

        assertEquals(Colour.GREEN, testBuffer.getScreenBackgroundColour());
        assertEquals(Colour.CYAN, testBuffer.getScreenForegroundColour());
    }

    void testDimensionSetters() {
        testBuffer.setHeight(1);
        testBuffer.setWidth(1);

        assertEquals(1, testBuffer.getHeight());
        assertEquals(1, testBuffer.getHeight());

    }

    /**
     * Cursor Tests
     */

    /**
     * Testing that setting x position of the cursor is clamped
     */
    @Test
    void testSetCursorX() {
        testBuffer.setCursorX(1);
        assertEquals(0, testBuffer.getCursorX());

        testBuffer.setCursorX(-1);
        assertEquals(0, testBuffer.getCursorX());
    }

    /**
     * Test that the cursor moves up and down correctly
     */
    @Test
    void testMoveY() {
        testBuffer.createNewLine();
        testBuffer.moveCursorY(-1);
        assertEquals(0, testBuffer.getCursorX());
        assertEquals(0, testBuffer.getCursorY());
    }

    @Test
    void testMoveX() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        assertEquals(0, testBuffer.getCursorX());
        assertEquals(0, testBuffer.getScreenCursorX());
    }

    /**
     * Test to make sure that cursor's x position moves when text is inserted
     */
    @Test void testInsertTextCursorPos() {
        testBuffer.insertText('a');
        assertEquals(1, testBuffer.getCursorX());
    }

    /**
     * Test to make sure that inserted text wraps
     */
    @Test
    void testInsertTextWrapLine() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText('a');
        }

        assertEquals("aa\na\n", testBuffer.printScreenContents());
    }

    @Test
    void testGetScreenLineWrapBehaviour() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');
        testBuffer.insertText('c');

        assertEquals("c\n", testBuffer.printScreenLine());
    }

    @Test
    void testScreenCursorCalculationAfterWrap() {
        for(int i = 0; i < 5; i++) {
            testBuffer.insertText('x');
        }

        assertTrue(testBuffer.getScreenCursorY() >= 0);
    }

    /**
     * Test that setting the Y cursor position updates the screen contents
     */
    void testSetCursorYScroll() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.setCursorY(0);
        assertEquals("0\n1\n", testBuffer.printScreenContents());

        testBuffer.setCursorY(2);
        assertEquals("1\n2\n", testBuffer.printScreenContents());
    }

    /**
     * Check that scrolling works
     */
    @Test
    void testCursorYScroll() {
        for(int i = 0; i < 3; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.scroll(-1);
        assertEquals("0\n1\n", testBuffer.printScreenContents());

        testBuffer.scroll(1);
        assertEquals("1\n2\n", testBuffer.printScreenContents());
    }

    @Test
    void testScrollClampUpper() {
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
        }

        testBuffer.scroll(100);

        assertTrue(testBuffer.getCursorY() <= 4);
    }

    @Test
    void testScrollClampLower() {
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
        }

        testBuffer.scroll(-100);

        assertEquals(0, testBuffer.getScreenCursorY());
    }

    /**
     * Testing that setting y position of the cursor is clamped
     */
    @Test
    void testSetCursorYClamped() {
        testBuffer.setCursorY(1);
        assertEquals(0, testBuffer.getCursorY());

        testBuffer.setCursorY(-1);
        assertEquals(0, testBuffer.getCursorY());
    }

    /**
     * Buffer manipulation tests
     */

    /**
     * Test that the cursor moves down when a new line is added
     */
    @Test
    void testCreateNewLine() {
        testBuffer.createNewLine();
        assertEquals(1, testBuffer.getCursorY());
        assertEquals(0, testBuffer.getCursorX());
    }

    /**
     * Test that lines beyond the scrollMaximum are removed
     */
    @Test
    void testCreateNewLinePastScrollMax() {
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText((char)('0' + i));
        }

        assertEquals("1\n2\n3\n4\n", testBuffer.printScrollbackContents());
    }

    @Test
    void testCreateNewLineNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.setCursorY(0);

        assertEquals(false, testBuffer.createNewLine());
    }

    @Test
    void testCreateNewLineFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);

        //Cursor should still be on the last logical line
        boolean result = testBuffer.createNewLine();

        assertFalse(result);
    }

    @Test
    void testInsertText() {
        testBuffer.insertText('a');

        assertEquals("a\n", testBuffer.printScreenContents());
    }

    @Test
    void testInsertTextNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.setCursorY(0);

        assertEquals(false, testBuffer.insertText('a'));
    }

    @Test
    void testInsertTextFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);

        assertFalse(testBuffer.insertText('a'));
    }

    @Test
    void testOverwriteText() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.overwriteText('b');

        assertEquals("b\n", testBuffer.printScreenContents());
    }

    @Test
    void testOverwriteTextNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setCursorY(0);

        assertEquals(false, testBuffer.overwriteText('b'));
    }

    @Test
    void testOverwriteTextFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);

        assertFalse(testBuffer.overwriteText('a'));
    }

    @Test
    void testSetBackgroundColour() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setBackgroundColourAtCursorPos(Colour.GREEN);

        assertEquals(Colour.GREEN, testBuffer.getBackgroundColourAtCursorPos());
    }

    @Test
    void testSetBackgroundColourNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setCursorY(0);

        assertEquals(false, testBuffer.setBackgroundColourAtCursorPos(Colour.GREEN));
    }

    @Test
    void testSetBackgroundColourFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText('a');
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);
        testBuffer.moveCursorX(-1);

        assertFalse(testBuffer.setBackgroundColourAtCursorPos(Colour.RED));
    }

    @Test
    void testSetBackgroundColourEndOfLine() {
        testBuffer.insertText('a');

        assertEquals(false, testBuffer.setBackgroundColourAtCursorPos(Colour.GREEN));
    }

    @Test
    void testSetForegroundColour() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setForegroundColourAtCursorPos(Colour.GREEN);

        assertEquals(Colour.GREEN, testBuffer.getForegroundColourAtCursorPos());
    }

    @Test
    void testSetForegroundColourNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setCursorY(0);

        assertEquals(false, testBuffer.setForegroundColourAtCursorPos(Colour.GREEN));
    }

    @Test
    void testSetForegroundColourFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText('a');
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);
        testBuffer.moveCursorX(-1);

        assertFalse(testBuffer.setForegroundColourAtCursorPos(Colour.RED));
    }

    @Test
    void testSetForegroundColourEndOfLine() {
        testBuffer.insertText('a');

        assertEquals(false, testBuffer.setForegroundColourAtCursorPos(Colour.GREEN));
    }

    @Test
    void testSetStyle() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setStyleAtCursorPos(Style.BOLD, true);

        assertTrue(testBuffer.getStyleAtCursorPos(Style.BOLD));
    }

    @Test
    void testSetStyleNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setCursorY(0);

        assertFalse(testBuffer.setStyleAtCursorPos(Style.BOLD, true));
    }

    @Test
    void testSetStyleFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText('a');
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);
        testBuffer.moveCursorX(-1);

        assertFalse(testBuffer.setStyleAtCursorPos(Style.BOLD, true));
    }

    @Test
    void testSetStyleEndOfLine() {
        testBuffer.insertText('a');

        assertFalse(testBuffer.setStyleAtCursorPos(Style.BOLD, true));
    }

    @Test
    void testFillLine() {
        testBuffer.fillLineWithChar('a');

        assertEquals("aa\n", testBuffer.printScreenLine());

        testBuffer.createNewLine();
        testBuffer.fillLineWithChar(new CharacterCell('a'));

        assertEquals("aa\n", testBuffer.printScreenLine());
    }

    @Test
    void testFillLineWithCharFromMiddle() {
        testBuffer.insertText('a');
        testBuffer.fillLineWithChar('b');

        assertEquals("ab\n", testBuffer.printScreenLine());
    }

    @Test
    void testFillLineWithCharacterCellFromMiddle() {
        testBuffer.insertText('a');
        testBuffer.fillLineWithChar(new CharacterCell('c'));

        assertEquals("ac\n", testBuffer.printScreenLine());
    }

    @Test
    void testFillLineNotOnBottom() {
        testBuffer.createNewLine();
        testBuffer.moveCursorY(-1);

        assertEquals(false, testBuffer.fillLineWithChar('a'));
        assertEquals(false, testBuffer.fillLineWithChar(new CharacterCell('a')));
    }

    @Test
    void testFillLineFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText('a');
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);

        assertFalse(testBuffer.fillLineWithChar('a'));
        assertFalse(testBuffer.fillLineWithChar(new CharacterCell('a')));
    }

    /**
     * Retrieving buffer content tests
     */

    @Test
    void testGetChar() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);

        assertEquals('a', testBuffer.getCharAtCursorPos());
    }

    @Test
    void testGetCharEndOfLine() {
        testBuffer.insertText('a');

        assertEquals(null, testBuffer.getCharAtCursorPos());
    }

    @Test
    void testGetBackgroundColour() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);

        assertEquals(Colour.BLACK, testBuffer.getBackgroundColourAtCursorPos());
    }

    @Test
    void testGetBackgroundColourEndOfLine() {
        testBuffer.insertText('a');

        assertEquals(Colour.DEFAULT, testBuffer.getBackgroundColourAtCursorPos());
    }

    @Test
    void testGetForegroundColour() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);

        assertEquals(Colour.WHITE, testBuffer.getForegroundColourAtCursorPos());
    }

    @Test
    void testGetForegroundColourEndOfLine() {
        testBuffer.insertText('a');

        assertEquals(Colour.DEFAULT, testBuffer.getForegroundColourAtCursorPos());
    }

    @Test
    void testDefaultColourFallback() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);

        assertEquals(Colour.BLACK, testBuffer.getBackgroundColourAtCursorPos());
        assertEquals(Colour.WHITE, testBuffer.getForegroundColourAtCursorPos());
    }

    @Test
    void testGetStyle() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setStyleAtCursorPos(Style.BOLD, true);

        assertTrue(testBuffer.getStyleAtCursorPos(Style.BOLD));
    }

    @Test
    void testGetStyleEndOfLine() {
        testBuffer.insertText('a');
        testBuffer.moveCursorX(-1);
        testBuffer.setStyleAtCursorPos(Style.BOLD, true);
        testBuffer.moveCursorX(1);

        assertTrue(Arrays.equals(new boolean[]{}, testBuffer.getStyleAtCursorPos()));
    }

    /**
     * Clearing the buffer tests
     */

    @Test
    void testClearLine() {
        testBuffer.insertText('a');
        testBuffer.clearLine();

        assertEquals("\n", testBuffer.printScrollLine());
    }

    @Test
    void testClearLineNotOnBottom() {
        testBuffer.insertText('a');
        testBuffer.createNewLine();
        testBuffer.moveCursorY(-1);
        testBuffer.clearLine();

        assertEquals("a\n", testBuffer.printScrollLine());
    }

    @Test
    void testClearLineFailsWhenScrolledUp() {
        //Create several lines so we have scrollback
        for(int i = 0; i < 5; i++) {
            testBuffer.createNewLine();
            testBuffer.insertText('a');
        }

        //Scroll up so bottomIndex is no longer at the bottom
        testBuffer.scroll(-1);

        assertFalse(testBuffer.clearLine());
    }

    /**
     * Check that clearing the screen and scrollback buffer works
     */
    @Test
    void testClearScrollback() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.clearEntireBuffer();

        assertEquals("\n", testBuffer.printScrollbackContents());
        assertEquals("\n", testBuffer.printScreenContents());
        assertEquals(0, testBuffer.getCursorX());
        assertEquals(0, testBuffer.getCursorY());
    }

    @Test
    void testClearScreen() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.clearScreen();

        assertEquals("012\n\n", testBuffer.printScrollbackContents());
        assertEquals("\n", testBuffer.printScreenContents());
        assertEquals(0, testBuffer.getScreenCursorX());
        assertEquals(0, testBuffer.getScreenCursorY());
    }

    @Test
    void testGetScrollLineMultipleChars() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');

        assertEquals("ab\n", testBuffer.printScrollLine());
    }

    /**
     * Testing Resizing
     */

    @Test
    void testSetHeightRebuildScreen() {
        testBuffer.insertText('a');
        testBuffer.createNewLine();
        testBuffer.insertText('b');

        testBuffer.setHeight(1);

        assertEquals(1, testBuffer.getHeight());
        assertTrue(testBuffer.printScreenContents().contains("b"));
    }

    @Test
    void testSetWidthRewrapLines() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');
        testBuffer.insertText('c');

        testBuffer.setWidth(1);

        assertEquals(1, testBuffer.getWidth());
        assertTrue(testBuffer.printScreenContents().contains("c"));
    }

    /**
     * Wide Character Handling
     */

    @Test
    void testInsertWideCharacter() {
        testBuffer.insertText('界'); // CJK wide char

        assertEquals(2, testBuffer.getCursorX());
        assertTrue(testBuffer.printScrollLine().contains("界"));
    }

    @Test
    void testInsertWideCharacterWrap() {
        testBuffer.insertText('界');
        testBuffer.insertText('界');

        assertTrue(testBuffer.printScreenContents().contains("界"));
    }

    @Test
    void testCursorCannotLandOnWideEnd() {
        testBuffer.insertText('界');

        testBuffer.setCursorX(1);

        assertEquals(0, testBuffer.getCursorX());
    }

    @Test
    void testMoveCursorAcrossWideChar() {
        testBuffer.insertText('界');

        testBuffer.moveCursorX(-1);

        assertEquals(0, testBuffer.getCursorX());
    }

    /**
     * deleteText()
     */

    @Test
    void testDeleteTextSingleCharacter() {
        testBuffer.insertText('a');

        testBuffer.deleteText();

        assertEquals("\n", testBuffer.printScrollLine());
    }

    @Test
    void testDeleteTextWideCharacter() {
        testBuffer.insertText('界');

        testBuffer.deleteText();

        assertEquals("\n", testBuffer.printScrollLine());
    }

    /**
     * Cursor Clamp Behaviour
     */

    @Test
    void testCursorXClampedToLineLength() {
        testBuffer.insertText('a');

        testBuffer.setCursorX(100);

        assertEquals(1, testBuffer.getCursorX());
    }

    @Test
    void testCursorXNegativeClamp() {
        testBuffer.insertText('a');

        testBuffer.setCursorX(-10);

        assertEquals(0, testBuffer.getCursorX());
    }


    /**
     * Screen vs Scrollback
     */

    @Test
    void testScreenAndScrollbackMatch() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');

        assertTrue(testBuffer.printScreenContents().contains("ab"));
        assertTrue(testBuffer.printScrollbackContents().contains("ab"));
    }


    /**
     * createNewLine Return Behaviour
     */

    @Test
    void testCreateNewLineReturnsTrue() {
        boolean result = testBuffer.createNewLine();

        assertTrue(result);
    }


    /**
     * Logical Line Wrapping Edge Cases
     */

    @Test
    void testLogicalLineWrapExactWidth() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');

        assertEquals("ab\n", testBuffer.printScreenLine());
    }

    @Test
    void testLogicalLineWrapOverflow() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');
        testBuffer.insertText('c');

        assertEquals("ab\nc\n", testBuffer.printScreenContents());
    }


    /**
     * Character Width Edge Cases
     */

    @Test
    void testControlCharacterIgnored() {
        boolean result = testBuffer.insertText('\u0007'); // bell

        assertFalse(result);
    }

    @Test
    void testCombiningCharacterIgnored() {
        boolean result = testBuffer.insertText('\u0301'); // combining acute accent

        assertFalse(result);
    }


    /**
     * Cursor Position After Resize
     */

    @Test
    void testCursorPositionAfterWidthChange() {
        testBuffer.insertText('a');
        testBuffer.insertText('b');

        testBuffer.setWidth(1);

        assertTrue(testBuffer.getScreenCursorY() >= 0);
    }

    @Test
    void testCursorPositionAfterHeightChange() {
        testBuffer.insertText('a');
        testBuffer.createNewLine();
        testBuffer.insertText('b');

        testBuffer.setHeight(1);

        assertTrue(testBuffer.getScreenCursorY() >= 0);
    }
}
