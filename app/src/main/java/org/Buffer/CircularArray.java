package org.Buffer;

import java.util.NoSuchElementException;
import java.util.ArrayDeque;

/**
 * Since the default java {@link ArrayDeque} does not support getting elements by index,
 * I have been forced to implement my own data structure which supports adding elements at
 * the front and back as well as direct array indexing.
 * The {@link CircularArray} is, as the name suggests, a circular array that supports
 * addition and removal at the front and back of the array, and getting any element by index
 */
public class CircularArray<T> {
    private Object[] data; //Where all of the elements go
    private int size; //Current number of elements in the CircularArray
    private int capacity; //Max number of elements possible in the current CircularArray's data buffer
    private int frontptr; //Pointer to the front of the circular array
    private int backptr; //Pointer to the back of the circular array

    /**
     * Constructor that allows the specification of the capacity of the CircularArray
     * Throws an {@link IllegalArgumentException} if the specified capacity is negative
     * @param initialCapacity the desired initial capacity of the CircularArray
     */
    public CircularArray(int initialCapacity) {
        if(initialCapacity < 0) //Capacity cannot be negative
            throw new IllegalArgumentException("Capacity must be non-negative");

        //Setting default values
        this.size = 0;
        this.capacity = initialCapacity;
        this.data = new Object[initialCapacity];
        this.frontptr = 0;
        this.backptr = 0;
    }

    /**
     * Constructor for the CircularArray that defaults the initial capacity to 16
     */
    public CircularArray() {
        this(16);
    }

    /**
     * Returns the element at the specified index of the CircularArray
     * Throws an {@link IndexOutOfBoundsException} if the provided index is less than 0 or greater than or equal to the CircularArray's size
     * @param index the index of the element we want
     * @return the element at the given index
     */
    public T get(int index) {
        if(index < 0 || index >= size) //Do not allow negative or out of bounds indecies
            throw new IndexOutOfBoundsException();

        return (T)this.data[(frontptr+index) % capacity];
    }

    /**
     * @return the size of the CircularArray
     */
    public int size() {
        return this.size;
    }

    /**
     * Moves the pointer to the front of the CircularArray one index to the left and then adds the new value at this new position
     * If the size of the array is >= the capacity of the array, it grows the array by a factor of 2
     * @param val the value to add
     */
    public void addToFront(T val) {
        if(this.size >= this.capacity) {
            grow();
        }

        this.frontptr = (this.frontptr - 1 + this.capacity) % this.capacity; //Modulo to find circular index
        this.data[this.frontptr] = val;
        this.size++;
    }

    /**
     * Adds the new value at the back of the CircularArray and moves the pointer to the back of the CircularArray one index to the right
     * If the size of the array is >= the capacity of the array, it grows the array by a factor of 2
     * @param val the value to add
     */
    public void addToBack(T val) {
        if(this.size >= this.capacity) {
            grow();
        }

        this.data[this.backptr] = val;
        this.backptr = (this.backptr + 1) % this.capacity; //Modulo to find circular index
        this.size++;
    }

    /**
     * Returns the value at the front of the CircularArray, setting its internal value to null and moving the pointer to the front
     * of the array one index to the right.
     * Throws a {@link NoSuchElementException} when called on an empty CircularArray
     * If the new size of the array is a quarter of the capacity, shrinks its size by half
     * @return the element at the front of the CircularArray
     */
    public T removeFromFront() {
        if(size == 0)
            throw new NoSuchElementException();

        T res = (T) this.data[this.frontptr]; //Store desired value
        this.data[this.frontptr] = null; //Set to null to encourage garbage collection
        this.frontptr = (this.frontptr + 1) % this.capacity;
        this.size--;

        //Shrink when capacity too small
        if(this.size < this.capacity / 4) {
            shrink();
        }

        return res;
    }

    /**
     * Moves the pointer to the front of the array one index to the left. Returns the value at the pointer to the back of the CircularArray,
     * setting its internal value to null
     * Throws a {@link NoSuchElementException} when called on an empty CircularArray
     * If the new size of the array is a quarter of the capacity, shrinks its size by half
     * @return the element at the front of the CircularArray
     */
    public T removeFromBack() {
        if(size == 0)
            throw new NoSuchElementException();

        this.backptr = (this.backptr - 1 + this.capacity) % this.capacity;
        T res = (T) this.data[this.backptr];
        this.data[this.backptr] = null;
        this.size--;

        if(this.size < this.capacity / 4) {
            shrink();
        }

        return res;
    }

    /**
     * Clears all of the elements in the CircularArray
     */
    public void clear() {
        this.data = new Object[capacity];
        this.size = 0;
        this.frontptr = 0;
        this.backptr = 0;
    }

    /**
     * Internal method to grow the size of the array by a factor of two
     * Copies all elements from the old buffer into the new buffer in sorted order
     */
    private void grow() {
        int newCapacity = capacity > 0 ? capacity * 2 : 1;
        Object newData[] = new Object[newCapacity];

        //Making sure that we copy the elements over so that the order is correct relative to the size of the new array
        for(int i = 0; i < this.size; i++) {
            int index = (this.frontptr + i) % this.capacity;
            newData[i] = this.data[index];
        }

        //Resetting size and pointer values
        this.data = newData;
        this.capacity = newCapacity;
        this.frontptr = 0;
        this.backptr = this.size;
    }

    /**
     * Internal method to shrink the size of the array by a factor of two
     * Copies all elements from the old buffer into the new buffer in sorted order
     */
    private void shrink() {
        int newCapacity = capacity > 1 ? capacity / 2 : 1;
        Object newData[] = new Object[newCapacity];

        //Making sure that we copy the elements over so that the order is correct relative to the size of the new array
        for(int i = 0; i < this.size; i++) {
            int index = (this.frontptr + i) % this.capacity;
            newData[i] = this.data[index];
        }

        //Resetting size and pointer values
        this.data = newData;
        this.capacity = newCapacity;
        this.frontptr = 0;
        this.backptr = this.size;
    }
}
