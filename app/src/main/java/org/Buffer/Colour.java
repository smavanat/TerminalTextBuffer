package org.Buffer;

/**
 * Enum to represent the 16 standard terminal colours using this as a reference: https://en.wikipedia.org/wiki/ANSI_escape_code#Colors
 * It also contains the special {@code DEFAULT} enum. This will tell the text buffer to use the current screen's background and foreground colours
 * when displaying the character
 */
public enum Colour {
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE,
    BRIGHT_BLACK,
    BRIGHT_RED,
    BRIGHT_GREEN,
    BRIGHT_YELLOW,
    BRIGHT_BLUE,
    BRIGHT_MAGENTA,
    BRIGHT_CYAN,
    BRIGHT_WHITE,
    DEFAULT
}
