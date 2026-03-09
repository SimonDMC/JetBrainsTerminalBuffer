package com.simondmc;

public class Main {
    public static void main(String[] args) {
        // Initialize terminal buffer
        TerminalBuffer terminal = new TerminalBuffer(19, 5, 5);

        // Fill screen with black background
        terminal.fillScreen(new CharacterCell.Builder().backgroundColor(TerminalColor.BLACK).build());

        // Move cursor to position
        terminal.moveCursor(CursorDirection.UP, 2);
        terminal.moveCursor(CursorDirection.RIGHT, 3);

        // Write text
        terminal.setForegroundColor(TerminalColor.GREEN);
        terminal.setBackgroundColor(TerminalColor.BLACK);
        terminal.writeText("Hello, ");
        terminal.setBold(true);
        terminal.writeText("World!");

        // Print the screen
        System.out.println(terminal.getScreen());
    }
}