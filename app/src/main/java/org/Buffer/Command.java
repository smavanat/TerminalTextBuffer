package org.Buffer;

/**
 * All of the possible commands that the {@link BufferIO} can interpret
 */
public enum Command {
    LEFT, //h
    RIGHT, //l
    DOWN, //j
    UP, //k
    INSERT, //i
    REPLACE, //r
    NEW_LINE, //n
    CLEAR_LINE, //cl
    CLEAR_SCREEN, //cs
    CLEAR_BUFFER, //cb
    PRINT_LINE, //pl
    PRINT_SCREEN, //ps
    PRINT_BUFFER, //pb
    SET_WIDTH, //sw
    SET_HEIGHT, //sh
    DELETE, //x
    NONE
}

