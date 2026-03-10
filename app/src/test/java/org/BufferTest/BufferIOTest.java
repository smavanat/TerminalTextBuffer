package org.BufferTest;

import static org.junit.jupiter.api.Assertions.*;

import org.Buffer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BufferIOTest {

    private BufferIO io;
    private TerminalBuffer buffer;

    @BeforeEach
    void setup() throws Exception {
        io = new BufferIO();

        Field bufField = BufferIO.class.getDeclaredField("buffer");
        bufField.setAccessible(true);
        buffer = (TerminalBuffer) bufField.get(io);
    }

    /**
     * parseIntArgument()
     */

    @Test
    void testParseIntArgument() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("parseIntArgument", String.class);
        m.setAccessible(true);

        int result = (int) m.invoke(io, "sw 50");

        assertEquals(50, result);
    }

    @Test
    void testParseIntArgumentClamped() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("parseIntArgument", String.class);
        m.setAccessible(true);

        int result = (int) m.invoke(io, "sw 200");

        assertEquals(80, result);
    }

    /**
     * getCommandFromInput()
     */

    @Test
    void testCommandLeft() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("getCommandFromInput", String.class);
        m.setAccessible(true);

        Command c = (Command) m.invoke(io, "h");

        assertEquals(Command.LEFT, c);
    }

    @Test
    void testCommandRight() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("getCommandFromInput", String.class);
        m.setAccessible(true);

        Command c = (Command) m.invoke(io, "l");

        assertEquals(Command.RIGHT, c);
    }

    @Test
    void testCommandInsert() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("getCommandFromInput", String.class);
        m.setAccessible(true);

        Command c = (Command) m.invoke(io, "i");

        assertEquals(Command.INSERT, c);
    }

    @Test
    void testCommandSetWidth() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("getCommandFromInput", String.class);
        m.setAccessible(true);

        Command c = (Command) m.invoke(io, "sw 20");

        assertEquals(Command.SET_WIDTH, c);
    }

    @Test
    void testCommandNone() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("getCommandFromInput", String.class);
        m.setAccessible(true);

        Command c = (Command) m.invoke(io, "unknown");

        assertEquals(Command.NONE, c);
    }

    /**
     * interpretCommand()
     */

    @Test
    void testInterpretMoveRight() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        m.setAccessible(true);

        buffer.insertText('a');

        m.invoke(io, "h");

        assertEquals(0, buffer.getCursorX());
    }

    @Test
    void testInterpretMoveLeft() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        m.setAccessible(true);

        buffer.insertText('a');

        m.invoke(io, "h");

        assertEquals(0, buffer.getCursorX());
    }

    @Test
    void testInterpretInsertMode() throws Exception {
        Method m = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        m.setAccessible(true);

        m.invoke(io, "i");

        Field modeField = BufferIO.class.getDeclaredField("currentMode");
        modeField.setAccessible(true);

        Mode mode = (Mode) modeField.get(io);

        assertEquals(Mode.INSERT, mode);
    }

    @Test
    void testInsertModeAddsText() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        Field modeField = BufferIO.class.getDeclaredField("currentMode");
        modeField.setAccessible(true);

        modeField.set(io, Mode.INSERT);

        interpret.invoke(io, "abc");

        assertTrue(buffer.printScrollLine().contains("abc"));
    }

    @Test
    void testInsertModeEscapeReturnsToCommand() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        Field modeField = BufferIO.class.getDeclaredField("currentMode");
        modeField.setAccessible(true);

        modeField.set(io, Mode.INSERT);

        interpret.invoke(io, "\u001B");

        Mode mode = (Mode) modeField.get(io);

        assertEquals(Mode.COMMAND, mode);
    }

    @Test
    void testReplaceMode() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        Field modeField = BufferIO.class.getDeclaredField("currentMode");
        modeField.setAccessible(true);

        buffer.insertText('a');
        buffer.moveCursorX(-1);

        modeField.set(io, Mode.REPLACE);

        interpret.invoke(io, "b");

        assertEquals("b\n", buffer.printScrollLine());
    }

    @Test
    void testDeleteCommand() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        buffer.insertText('a');
        buffer.moveCursorX(-1);

        interpret.invoke(io, "x");

        assertEquals("\n", buffer.printScrollLine());
    }

    /**
     * Resize commands
     */

    @Test
    void testSetWidthCommand() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        interpret.invoke(io, "sw 5");

        assertEquals(5, buffer.getWidth());
    }

    @Test
    void testSetHeightCommand() throws Exception {

        Method interpret = BufferIO.class.getDeclaredMethod("interpretCommand", String.class);
        interpret.setAccessible(true);

        interpret.invoke(io, "sh 6");

        assertEquals(6, buffer.getHeight());
    }

}
