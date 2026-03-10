package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.Buffer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TerminalLineTest {
    private TerminalLine testLine;

    @BeforeEach
    void initialise() {
        testLine = new TerminalLine(10, true);
    }

    @Test
    void testConstructorDefaults() {
        testLine = new TerminalLine(5);

        assertFalse(testLine.getWrapped());
        assertEquals(new ArrayList<CharacterCell>(), testLine.getCharacters());
    }

    @Test
    void testConstructorSetWrapped() {
        assertTrue(testLine.getWrapped());
    }

    @Test
    void testAdd() {
        testLine.add(new CharacterCell('c'));

        assertEquals(new CharacterCell('c'), testLine.get(0));
    }

    @Test
    void testAddMultiple() {
        for(int i = 0; i < 11; i++) {
            testLine.add(new CharacterCell('c'));
        }

        assertEquals(10, testLine.size());
    }

    @Test
    void testAddIndex() {
        testLine.add(0, new CharacterCell('c'));

        assertEquals(new CharacterCell('c'), testLine.get(0));
    }

    @Test
    void testAddIndexOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> testLine.add(-1, new CharacterCell('c')));
        assertThrows(IndexOutOfBoundsException.class, () -> testLine.add(1, new CharacterCell('c')));
    }

    @Test
    void testAddIndexMultiple() {
        for(int i = 0; i < 11; i++) {
            testLine.add(testLine.size(), new CharacterCell('c'));
        }

        assertEquals(10, testLine.size());
    }

    @Test
    void testGetOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> testLine.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> testLine.get(1));
    }

    @Test
    void testClear() {
        for(int i = 0; i < 10; i++) {
            testLine.add(testLine.size(), new CharacterCell('c'));
        }
        testLine.clear();

        assertEquals(0, testLine.size());
    }

    @Test
    void testAddAll() {
        ArrayList<CharacterCell> testList = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            testList.add(testList.size(), new CharacterCell('c'));
        }

        testLine.addAll(testList);
        assertEquals(testList, testLine.getCharacters());
    }
}
