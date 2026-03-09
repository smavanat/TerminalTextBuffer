package org.BufferTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;

import org.Buffer.CircularArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CircularArrayTest {
    private CircularArray<Integer> testArray;

    @BeforeEach
    void initialise() {
        testArray = new CircularArray<>(4);
    }

    /**
     * Constructor tests
     */

    /**
     * Tests that the default constructor works
     */
    @Test
    void testDefaultConstructor() {
        testArray = new CircularArray<>();
        assertEquals(0, testArray.size());
    }

    /**
     * Tests that the parameterised constructor works
     */
    @Test
    void testCapacityConstructor() {
        assertEquals(0, testArray.size());
    }

    /**
     * Tests that the parameterised constructor throws an exception with negative number arguments
     */
    @Test
    void testCapacityConstructorIllegalArgs() {
        assertThrows(IllegalArgumentException.class, () -> new CircularArray<Integer>(-1));
    }

    /**
     * CircularArray.get() tests
     */

    /**
     * Test that get works when elements are added to the array
     */
    @Test
    void testGetValid() {
        testArray.addToBack(1);
        testArray.addToBack(2);
        assertEquals(1, testArray.get(0));
        assertEquals(2, testArray.get(1));
    }

    /**
     * Test that get works when the input index is negative
     */
    @Test
    void testGetNegativeIndex() {
        testArray.addToBack(1);
        assertThrows(IndexOutOfBoundsException.class, () -> testArray.get(-1));
    }

    /**
     * Test that get works when the input index is out of bounds
     */
    @Test
    void testGetIndexTooLarge() {
        assertThrows(IndexOutOfBoundsException.class, () -> testArray.get(0));
    }

    /**
     * CircularArray.addToFront() tests
     */

    /**
     * Test that addToFront can correctly add an element
     */
    @Test
    void testAddToFront() {
        testArray.addToFront(10);
        assertEquals(10, testArray.get(0));
    }

    /**
     * Test that addToFront can correctly add several elements and trigger grow()
     */
    @Test
    void testAddToFrontFive() {
        for(int i = 0; i < 5; i++) {
            testArray.addToFront(i);
        }

        for(int i = 0; i < 5; i++) {
            assertEquals(5-i-1, testArray.get(i));
        }
    }

    /**
     * CircularArray.addToBack() tests
     */

    /**
     * Test that addToBack can correctly add one element
     */
    @Test
    void testAddToBack() {
        testArray.addToBack(10);
        assertEquals(10, testArray.get(0));
    }

    /**
     * Test that addToBack can correctly add several elements and trigger grow()
     */
    @Test
    void testAddToBackFive() {
        for(int i = 0; i < 5; i++) {
            testArray.addToBack(i);
        }

        for(int i = 0; i < 5; i++) {
            assertEquals(i, testArray.get(i));
        }
    }

    /**
     * CircularArray.removeFromFront() tests
     */

    /**
     * Test that removeFromFront works when the element was added to the front
     */
    @Test
    void testremoveFromFrontAddFront() {
        testArray.addToFront(10);
        assertEquals(10, testArray.removeFromFront());
        assertEquals(0, testArray.size());
    }

    /**
     * Test that removeFromFront works when the element was added to the back
     */
    @Test
    void testremoveFromFrontAddBack() {
        testArray.addToBack(10);
        assertEquals(10, testArray.removeFromFront());
        assertEquals(0, testArray.size());
    }

    /**
     * Test that removeFromFront throws an exception when the array is empty
     */
    @Test
    void testRemoveFromFrontEmpty() {
        assertThrows(NoSuchElementException.class, testArray::removeFromFront);
    }

    /**
     * Test that shrink triggers successfully when the array gets significantly smaller
     */
    @Test
    void testShrinkFront() {
        CircularArray<Integer> arr = new CircularArray<>(8);

        for(int i = 0; i < 8; i++) {
            arr.addToFront(i);
        }

        for(int i = 0; i < 7; i++) {
            arr.removeFromFront(); // should trigger shrink
        }

        assertEquals(1, arr.size());
    }

    /**
     * CircularArray.removeFromBack() tests
     */

    /**
     * Test that removeFromBack works when the element was added to the front
     */
    @Test
    void testremoveFromBackAddFront() {
        testArray.addToFront(10);
        assertEquals(10, testArray.removeFromBack());
        assertEquals(0, testArray.size());
    }

    /**
     * Test that removeFromBack works when the element was added to the back
     */
    @Test
    void testremoveFromBackAddBack() {
        testArray.addToBack(10);
        assertEquals(10, testArray.removeFromBack());
        assertEquals(0, testArray.size());
    }

    /**
     * Test that removeFromBack throws an exception when the array is empty
     */
    @Test
    void testRemoveFromBackEmpty() {
        assertThrows(NoSuchElementException.class, testArray::removeFromBack);
    }

    /**
     * Test that shrink triggers successfully when the array gets significantly smaller
     */
    @Test
    void testShrinkBack() {
        testArray = new CircularArray<>(8);

        for(int i = 0; i < 8; i++) {
            testArray.addToBack(i);
        }

        for(int i = 0; i < 7; i++) {
            testArray.removeFromBack(); // should trigger shrink
        }

        assertEquals(1, testArray.size());
    }

    /**
     * Test that the clear function removes everything from the CircularArray
     */
    @Test
    void testClear() {
        for(int i = 0; i < 4; i++) {
            testArray.addToFront(i);
        }

        testArray.clear();

        assertEquals(0, testArray.size());
    }

    /**
     * Test to make sure wrap around logic works
     */
    @Test
    void testWrapAround() {
        testArray = new CircularArray<>(4);

        testArray.addToBack(1);
        testArray.addToBack(2);
        testArray.addToBack(3);

        testArray.removeFromFront();
        testArray.removeFromFront();

        testArray.addToBack(4);
        testArray.addToBack(5);

        assertEquals(3, testArray.get(0));
        assertEquals(4, testArray.get(1));
        assertEquals(5, testArray.get(2));
    }
}
