package org.Buffer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class BufferIO {
    private TerminalBuffer buffer;
    private Scanner inputScanner;
    private ArrayList<CharacterCell> screenBuf;
    private Mode currentMode = Mode.COMMAND;
    private String message;
    private InputStreamReader reader;

    public BufferIO() {
        buffer = new TerminalBuffer(10, 10, -1, Colour.WHITE, Colour.BLACK);
        inputScanner = new Scanner(System.in);
        message = "";
        reader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    }

    public void getInputFromUser() {
        buffer.setWidth(getIntInputBound("Enter Terminal Width (must be between 0 and 80): ", 0, 80));
        buffer.setHeight(getIntInputBound("Enter Terminal Height (must be between 0 and 24): ", 0, 24));
        buffer.setScrollMaximum(getIntInput("Enter Terminal Scroll Maximum (negative input will be taken to mean unbounded): "));

        initialiseScreen();
    }

    public void mainLoop() {
        while(true) {
            clearScreen();
            copyScreen();
            printScreen();
            // try { Thread.sleep(1000); } catch (InterruptedException e) {}
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

    private void printScreen() {
        System.out.println("MODE: " + currentMode.toString() + "                 Position: (" + buffer.getCursorX() + ", " + buffer.getCursorY() + ")");
        for(int i = 0; i < screenBuf.size(); i++) {
            if(screenBuf.get(i).getTrailFlag() == TrailFlag.WIDE_END) continue;
            if(buffer.getScreenCursorY() * buffer.getWidth() + buffer.getScreenCursorX() == i) printCursor();
            else printCell(screenBuf.get(i));
            if((i + 1) % buffer.getWidth() == 0) System.out.print('\n');
        }
        if(message != "") System.out.print(message);
        message ="";
    }

    private void initialiseScreen() {
        screenBuf = new ArrayList<>(buffer.getWidth() * buffer.getHeight());
        for(int i = 0; i < buffer.getWidth() * buffer.getHeight(); i++) {
            screenBuf.add(new CharacterCell(' ', buffer.getScreenForegroundColour(), buffer.getScreenBackgroundColour(), Style.NONE));
        }
    }

    private void clearScreen() {
        // System.out.print("\u001B[2J");   //clear the screen
        // System.out.print("\u001B[H");    //move cursor to top-left corner
        System.out.flush();

        for(int i = 0; i < screenBuf.size(); i++) {
            screenBuf.get(i).setCharacter(' ');
            screenBuf.get(i).setForegroundColour(buffer.getScreenForegroundColour());
            screenBuf.get(i).setBackgroundColour(buffer.getScreenBackgroundColour());
            screenBuf.get(i).setStyleFlag(Style.NONE);
            screenBuf.get(i).setTrailFlag(TrailFlag.NORMAL);
        }
    }

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

    private void printCursor() {
        printCell(new CharacterCell(' ', Colour.RED, Colour.RED, Style.NONE));
    }

    private void printCell(CharacterCell c) {
        Colour fc = c.getForegroundColour() == Colour.DEFAULT ? buffer.getScreenForegroundColour() : c.getForegroundColour();
        Colour bc = c.getBackgroundColour() == Colour.DEFAULT ? buffer.getScreenBackgroundColour() : c.getBackgroundColour();
        System.out.print(getAnsiStyle(c.getStyleFlag()) + getAnsiForegroundColour(fc) + getAnsiBackgroundColour(bc) + c.getCharacter().charValue() + "\u001B[0m");
    }

    private String getAnsiStyle(Style s) {
        switch(s) {
            case BOLD:
                return "\u001B[1m";
            case UNDERLINE:
                return "\u001B[4m";
            case ITALIC:
                return "\u001B[3m";
            default:
                return "";
        }
    }

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

    private Command getCommandFromInput(String command) {
        if(command.length() >= 2 && command.substring(0, 2).equals("sw")) return Command.SET_WIDTH;
        if(command.length() >= 2 && command.substring(0, 2).equals("sh")) return Command.SET_HEIGHT;
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
                    default:
                        break;
                }
                break;
            case INSERT:
                if(command.charAt(0)== 27) {
                    currentMode = Mode.COMMAND;
                    break;
                }
                // for(int i = 0; i < command.length(); i++) {
                //     buffer.insertText(command.charAt(i));
                // }
                command.codePoints().forEach(cp -> buffer.insertText((char) cp));
                break;
            case REPLACE:
                if(command.charAt(0) == 27) currentMode = Mode.COMMAND;
                else buffer.overwriteText(command.charAt(0));
                break;
        }
    }

    private int parseIntArgument(String command) {
        return Math.max(0, Math.min(Integer.parseInt(command.substring(3).trim()), 80));
    }
}
