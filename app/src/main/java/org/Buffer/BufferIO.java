package org.Buffer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class to act as input manager for the {@link TerminalBuffer}
 * Operates using a series of user commands, similar to vim
 */
public class BufferIO {
    private TerminalBuffer buffer; //The buffer this is getting I/O for
    private Scanner inputScanner; //The scanner to get user input
    private InputStreamReader reader; //The reader to get UTF8 input
    private ArrayList<CharacterCell> screenBuf; //Where we output the text buffer's screen
    private Mode currentMode = Mode.COMMAND; //The state this class is in
    private String message; //Any output we may need to print to the user

    /**
     * Default constructor
     * Sets the screen size to be 10x10, infinite scrollback, white background and black foreground
     */
    public BufferIO() {
        buffer = new TerminalBuffer(10, 10, -1, Colour.WHITE, Colour.BLACK);
        inputScanner = new Scanner(System.in);
        message = "";
        reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    }

    /**
     * Function to poll the user for input
     */
    public void getInputFromUser() {
        buffer.setWidth(getIntInputBound("Enter Terminal Width (must be between 0 and 80): ", 0, 80));
        buffer.setHeight(getIntInputBound("Enter Terminal Height (must be between 0 and 24): ", 0, 24));
        buffer.setScrollMaximum(getIntInput("Enter Terminal Scroll Maximum (negative input will be taken to mean unbounded): "));

        initialiseScreen();
    }

    /**
     * The main execution loop after the user has provided input
     */
    public void mainLoop() {
        while(true) {
            clearScreen();
            copyScreen();
            printScreen();
            String command = "";
            try {
                command = readUserCommands();
            }
            catch (IOException e){
                System.out.println("Error: " + e.getMessage());
            }

            interpretCommand(command);
        }
    }

    /**
     * Prints the terminal screen out alongside some statistics
     */
    private void printScreen() {
        System.out.println("MODE: " + currentMode.toString() + "                 Position: (" + buffer.getCursorX() + ", " + buffer.getCursorY() + ")"); //Print the mode and cursor pos

        for(int i = 0; i < screenBuf.size(); i++) {
            if(screenBuf.get(i).getTrailFlag() == TrailFlag.WIDE_END) continue; //Skip dummy chars
            if((buffer.getScreenCursorY() * buffer.getWidth()) + buffer.getScreenCursorX() == i) printCursor(); //Print the cell to the screen if there is one
            else printCell(screenBuf.get(i)); //Otherwise just print a blank screen
            if((i + 1) % buffer.getWidth() == 0) System.out.print('\n'); //Newline once we reach the end of a line
        }
        if(message != "") System.out.print(message); //Print and clear the message if there is one
        message ="";
    }

    /**
     * Resets this classes screen buffer size to match that of the text buffer
     */
    private void initialiseScreen() {
        screenBuf = new ArrayList<>(buffer.getWidth() * buffer.getHeight());
        for(int i = 0; i < buffer.getWidth() * buffer.getHeight(); i++) {
            screenBuf.add(new CharacterCell(' ', buffer.getScreenForegroundColour(), buffer.getScreenBackgroundColour(), new boolean[]{false, false, false}));
        }
    }

    /**
     * Clear's this class's internal screen buffer, as well as the actual terminal screen we are printing to
     */
    private void clearScreen() {
        System.out.print("\u001B[2J");   //clear the screen
        System.out.print("\u001B[H");    //move cursor to top-left corner
        System.out.flush();

        //Set all of the chars to be blank and default
        for(int i = 0; i < screenBuf.size(); i++) {
            screenBuf.get(i).setCharacter(' ');
            screenBuf.get(i).setForegroundColour(buffer.getScreenForegroundColour());
            screenBuf.get(i).setBackgroundColour(buffer.getScreenBackgroundColour());
            screenBuf.get(i).setAllStyleFlags(new boolean[]{false, false, false});
            screenBuf.get(i).setTrailFlag(TrailFlag.NORMAL);
        }
    }

    /**
     * Copy's the text buffers screen into our buffer
     */
    private void copyScreen() {
        for(int i = 0; i < buffer.getScreenSize(); i++) {
            TerminalLine line = buffer.getScreenLine(i);
            for(int j = 0; j < line.size(); j++) {
                CharacterCell screenChar = screenBuf.get(i * buffer.getWidth() + j);
                CharacterCell bufChar = line.get(j);
                screenChar.copy(bufChar);
            }
        }
    }

    /**
     * Prints a red cursor
     */
    private void printCursor() {
        printCell(new CharacterCell(' ', Colour.RED, Colour.RED, new boolean[]{false, false, false}));
    }

    /**
     * Prints a cell including its styling and colours
     * @param c the CharacterCell to print
     */
    private void printCell(CharacterCell c) {
        Colour fc = c.getForegroundColour() == Colour.DEFAULT ? buffer.getScreenForegroundColour() : c.getForegroundColour();
        Colour bc = c.getBackgroundColour() == Colour.DEFAULT ? buffer.getScreenBackgroundColour() : c.getBackgroundColour();
        System.out.print(getAnsiStyle(c.getAllStyleFlags()) + getAnsiForegroundColour(fc) + getAnsiBackgroundColour(bc) + c.getCharacter().charValue() + "\u001B[0m");
    }

    /**
     * @param styles the styles to get the ansi codes for
     * @return the ANSI code for the styles given a cell as a String
     */
    private String getAnsiStyle(boolean styles[]) {
        String ret = "";
        if(styles[Style.BOLD.ordinal()]) ret += "\u001B[1m";
        if(styles[Style.UNDERLINE.ordinal()]) ret += "\u001B[4m";
        if(styles[Style.ITALIC.ordinal()]) ret += "\u001B[3m";
        return ret;
    }

    /**
     * @param c the Colour whose ANSI code needs to be retrieved
     * @return the ANSI code corresponding to the given background colour as a string
     */
    private String getAnsiBackgroundColour(Colour c) {
        switch(c) {
            case BLACK:
                return "\u001B[40m";
            case RED:
                return "\u001B[41m";
            case GREEN:
                return "\u001B[42m";
            case YELLOW:
                return "\u001B[43m";
            case BLUE:
                return "\u001B[44m";
            case MAGENTA:
                return "\u001B[45m";
            case CYAN:
                return "\u001B[46m";
            case WHITE:
                return "\u001B[47m";
            case BRIGHT_BLACK:
                return "\u001B[100m";
            case BRIGHT_RED:
                return "\u001B[101m";
            case BRIGHT_GREEN:
                return "\u001B[102m";
            case BRIGHT_YELLOW:
                return "\u001B[103m";
            case BRIGHT_BLUE:
                return "\u001B[104m";
            case BRIGHT_MAGENTA:
                return "\u001B[105m";
            case BRIGHT_CYAN:
                return "\u001B[106m";
            case BRIGHT_WHITE:
                return "\u001B[107m";
            default:
                return "";
        }
    }

    /**
     * @param c the Colour whose ANSI code needs to be retrieved
     * @return the ANSI code corresponding to the given foreground colour as a string
     */
    private String getAnsiForegroundColour(Colour c) {
        switch(c) {
            case BLACK:
                return "\u001B[30m";
            case RED:
                return "\u001B[31m";
            case GREEN:
                return "\u001B[32m";
            case YELLOW:
                return "\u001B[33m";
            case BLUE:
                return "\u001B[34m";
            case MAGENTA:
                return "\u001B[35m";
            case CYAN:
                return "\u001B[36m";
            case WHITE:
                return "\u001B[37m";
            case BRIGHT_BLACK:
                return "\u001B[90m";
            case BRIGHT_RED:
                return "\u001B[91m";
            case BRIGHT_GREEN:
                return "\u001B[92m";
            case BRIGHT_YELLOW:
                return "\u001B[93m";
            case BRIGHT_BLUE:
                return "\u001B[94m";
            case BRIGHT_MAGENTA:
                return "\u001B[95m";
            case BRIGHT_CYAN:
                return "\u001B[96m";
            case BRIGHT_WHITE:
                return "\u001B[97m";
            default:
                return "";
        }
    }

    /**
     * Reads commands from the user including wide chars properly
     */
    private String readUserCommands() throws IOException {
        StringBuilder inputBuffer = new StringBuilder();
        System.out.flush();

        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '\n') break;
            else if (ch == '\r') continue;
            else if (ch == 127 || ch == 8) { // backspace
                if (inputBuffer.length() > 0) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                    System.out.print("\b \b");
                }
            }
            else {
                inputBuffer.append((char) ch);
            }
            System.out.flush();
        }

        return inputBuffer.toString();
    }

    /**
     * Keeps polling the user for int input
     * @param promptText what the user should be displayed as a prompt
     * @return the int value they enter
     */
    private int getIntInput(String promptText) {
        System.out.print(promptText);
        while(true) {
            if(inputScanner.hasNextInt()) {
                return inputScanner.nextInt();
            }
            else if(inputScanner.hasNext()){
                System.out.println("Invalid integer input");
                inputScanner.next();
                inputScanner.nextLine();
            }
        }
    }

    /**
     * Keeps polling the user for int input within a given bound
     * @param promptText what the user should be displayed as a prompt
     * @return the int value they enter
     */
    private int getIntInputBound(String promptText, int lower, int upper) {
        System.out.print(promptText);
        while(true) {
            if(inputScanner.hasNextInt()) {
                int ret = inputScanner.nextInt();
                inputScanner.nextLine();

                if(ret >= lower && ret <= upper) {
                    return ret;
                }
                else {
                    System.out.println("Input must be between " + lower + " and " + upper);
                }
            }
            else if(inputScanner.hasNext()){
                System.out.println("Invalid integer input");
                inputScanner.next();
                inputScanner.nextLine();
            }
        }
    }

    /**
     * Parses a string for a Command
     * @param command the string to parse
     * @return the parsed command
     */
    private Command getCommandFromInput(String command) {
        if(command.length() >= 2 && command.substring(0, 2).equals("sw")) return Command.SET_WIDTH;
        if(command.length() >= 2 && command.substring(0, 2).equals("sh")) return Command.SET_HEIGHT;
        if(command.length() >= 2 && command.substring(0, 1).equals("f")) return Command.FILL;
        switch(command) {
            case "h":
                return Command.LEFT;
            case "l":
                return Command.RIGHT;
            case "j":
                return Command.DOWN;
            case "k":
                return Command.UP;
            case "i":
                return Command.INSERT;
            case "r":
                return Command.REPLACE;
            case "n":
                return Command.NEW_LINE;
            case "cl":
                return Command.CLEAR_LINE;
            case "cs":
                return Command.CLEAR_SCREEN;
            case "cb":
                return Command.CLEAR_BUFFER;
            case "pl":
                return Command.PRINT_LINE;
            case "ps":
                return Command.PRINT_SCREEN;
            case "pb":
                return Command.PRINT_BUFFER;
            case "x":
                return Command.DELETE;
            default:
                return Command.NONE;
        }
    }

    /**
     * Executes the function associated with a command
     * @param command the string containing the command to execute
     */
    private void interpretCommand(String command) {
        if(command.length() <= 0) return;

        switch(currentMode) {
            case COMMAND:
                Command c = getCommandFromInput(command);

                switch(c) {
                    case UP:
                        buffer.moveCursorY(-1);
                        break;
                    case DOWN:
                        buffer.moveCursorY(1);
                        break;
                    case LEFT:
                        buffer.moveCursorX(-1);
                        break;
                    case RIGHT:
                        buffer.moveCursorX(1);
                        break;
                    case INSERT:
                        currentMode = Mode.INSERT;
                        break;
                    case REPLACE:
                        currentMode = Mode.REPLACE;
                        break;
                    case NEW_LINE:
                        buffer.createNewLine();
                        break;
                    case CLEAR_LINE:
                        buffer.clearLine();
                        break;
                    case CLEAR_SCREEN:
                        buffer.clearScreen();
                        break;
                    case CLEAR_BUFFER:
                        buffer.clearEntireBuffer();
                        break;
                    case PRINT_LINE:
                        message = buffer.printScrollLine();
                        break;
                    case PRINT_SCREEN:
                        message = buffer.printScreenContents();
                        break;
                    case PRINT_BUFFER:
                        message = buffer.printScrollbackContents();
                        break;
                    case SET_WIDTH:
                        if (command.length() < 4) break;
                        buffer.setWidth(parseIntArgument(command));
                        initialiseScreen();
                        break;
                    case SET_HEIGHT:
                        if (command.length() < 4) break;
                        buffer.setHeight(parseIntArgument(command));
                        initialiseScreen();
                        break;
                    case DELETE:
                        buffer.deleteText();
                        break;
                    case FILL:
                        char fillChar = parseCharArgs(command);
                        if(fillChar != ' ') buffer.fillLineWithChar(fillChar);
                    default:
                        break;
                }
                break;
            case INSERT:
                if(command.charAt(0)== 27) {
                    currentMode = Mode.COMMAND;
                    break;
                }
                command.codePoints().forEach(cp -> buffer.insertText((char) cp));
                break;
            case REPLACE:
                if(command.charAt(0) == 27) currentMode = Mode.COMMAND;
                else buffer.overwriteText(command.charAt(0));
                break;
        }
    }

    /**
     * @param command the string whose int argument needs to be parsed
     * @return the int argument given with a command
     */
    private int parseIntArgument(String command) {
        return Math.max(0, Math.min(Integer.parseInt(command.substring(3).trim()), 80));
    }

    private char parseCharArgs(String command) {
        if(command.startsWith("f ") && command.length() > 2) {
            return command.charAt(2);
        }
        return ' ';
    }
}
