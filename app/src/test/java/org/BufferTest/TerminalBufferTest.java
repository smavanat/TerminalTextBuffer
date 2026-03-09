package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.Buffer.TerminalBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TerminalBufferTest {
    private TerminalBuffer testBuffer;

    @BeforeEach
    void initialise() {
        testBuffer = new TerminalBuffer(2, 2, 2);
    }

    /**
     * Test that initial cursor positions are in top-left
     */
    @Test
    void testCursorStart() {
        assertEquals(0, testBuffer.getCursorX());
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
    void testAddText() {

    }
}
