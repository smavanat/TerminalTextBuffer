package org.Buffer;

public class Main {
    public static void main(String args[]) {
        BufferIO terminal = new BufferIO();

        terminal.getInputFromUser();
        terminal.mainLoop();
    }
}
