package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.Buffer.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CharacterCellTest {
    private CharacterCell testCell;

    @BeforeEach
    void initialise() {
        testCell = new CharacterCell('c');
    }

    @Test
    void testEquals() {
        assertTrue(testCell.equals(testCell));
        assertTrue(testCell.equals(new CharacterCell('c')));
    }

    @Test
    void testNotEquals() {
        assertFalse(testCell.equals(null));
        assertFalse(testCell.equals(new Object()));
        assertFalse(testCell.equals(new CharacterCell('a')));
        assertFalse(testCell.equals(new CharacterCell('c', Colour.WHITE, Colour.DEFAULT, Style.NONE)));
        assertFalse(testCell.equals(new CharacterCell('c', Colour.DEFAULT, Colour.WHITE, Style.NONE)));
        assertFalse(testCell.equals(new CharacterCell('c', Colour.DEFAULT, Colour.DEFAULT, Style.BOLD)));
    }

    @Test
    void testSetTrailFlag() {
        testCell.setTrailFlag(TrailFlag.WIDE_START);
        assertEquals(TrailFlag.WIDE_START, testCell.getTrailFlag());
    }

    @Test
    void testCopy() {
        CharacterCell original = new CharacterCell('a', Colour.WHITE, Colour.BLACK, Style.BOLD, TrailFlag.WIDE_END);
        testCell.copy(original);
        assertEquals(original, testCell);
    }
}
