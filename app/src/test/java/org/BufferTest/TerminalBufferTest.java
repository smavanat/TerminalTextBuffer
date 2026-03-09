package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.Buffer.TerminalBuffer;
import org.Buffer.Colour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TerminalBufferTest {
    private TerminalBuffer testBuffer;

    @BeforeEach
    void initialise() {
        testBuffer = new TerminalBuffer(5, 3, 10);
    }

    /**
     * ConstructorTests
     */

    /**
     * Test defaults
     */
    @Test
    void testConstructorDefaults() {
        assertEquals(5, testBuffer.getWidth());
        assertEquals(3, testBuffer.getHeight());
        assertEquals(10, testBuffer.getScrollMaximum());
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

        assertEquals("1\n2\n3\n4\n", testBuffer.getScrollbackContents());
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
     * Test to make sure that text is inserted correctly
     */
    @Test
    void testInsertText() {
        testBuffer.insertText('a');

        assertEquals("a\n", testBuffer.getScreenContents());
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

        assertEquals("aa\na\n", testBuffer.getScreenContents());
    }

    /**
     * Test that setting the Y cursor position updates the screen contents
     */
    void testSetCursorYScroll() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.setCursorY(0);
        assertEquals("0\n1\n", testBuffer.getScreenContents());

        testBuffer.setCursorY(2);
        assertEquals("1\n2\n", testBuffer.getScreenContents());
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
        assertEquals("0\n1\n", testBuffer.getScreenContents());

        testBuffer.scroll(1);
        assertEquals("1\n2\n", testBuffer.getScreenContents());
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

        assertEquals("\n", testBuffer.getScrollbackContents());
        assertEquals("\n", testBuffer.getScreenContents());
        assertEquals(0, testBuffer.getCursorX());
        assertEquals(0, testBuffer.getCursorY());
    }

    /**
     * Tests clearing the screen
     */
    @Test
    void testClearScreen() {
        for(int i = 0; i < 3; i++) {
            testBuffer.insertText((char)('0' + i));
        }

        testBuffer.clearScreen();

        assertEquals("012\n\n", testBuffer.getScrollbackContents());
        assertEquals("\n", testBuffer.getScreenContents());
        assertEquals(0, testBuffer.getScreenCursorX());
        assertEquals(0, testBuffer.getScreenCursorY());
    }
}
