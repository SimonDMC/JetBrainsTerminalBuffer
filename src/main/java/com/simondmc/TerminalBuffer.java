package com.simondmc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TerminalBuffer {
    // The screen and scrollback are represented by a LinkedList of CharacterCell[width].
    //
    // This means that adding a new line (and perhaps removing the oldest one) is an O(1)
    // operation while maintaining an O(n) printing of all lines. The simplicity of this
    // solution comes at the expense of O(n) arbitrary line retrieval as opposed to O(1).
    private final List<CharacterCell[]> lines = new LinkedList<>();
    private final int screenWidth;
    private final int screenHeight;
    private final int scrollbackHeight;
    private int cursorRow;
    private int cursorCol;

    private TerminalColor foregroundColor = TerminalColor.NONE;
    private TerminalColor backgroundColor = TerminalColor.NONE;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;

    public TerminalBuffer(int screenWidth, int screenHeight, int scrollbackHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scrollbackHeight = scrollbackHeight;

        // Fill with empty lines
        for (int i = 0; i < screenHeight + scrollbackHeight; i++) {
            CharacterCell[] emptyLine = new CharacterCell[screenWidth];
            Arrays.fill(emptyLine, new CharacterCell());
            lines.addFirst(emptyLine);
        }

        cursorRow = 0;
        cursorCol = 0;
    }

    public int getCursorRow() {
        return cursorRow;
    }

    public void setCursorRow(int cursorRow) {
        if (cursorRow < 0 || cursorRow >= screenHeight) {
            throw new IllegalArgumentException("Cannot move cursor out of bounds");
        }

        this.cursorRow = cursorRow;
    }

    public int getCursorCol() {
        return cursorCol;
    }

    public void setCursorCol(int cursorCol) {
        if (cursorCol < 0 || cursorCol >= screenWidth) {
            throw new IllegalArgumentException("Cannot move cursor out of bounds");
        }

        this.cursorCol = cursorCol;
    }

    /**
     * Move cursor in a specific direction
     *
     * @param direction direction to move in
     * @param units     how many characters to move by
     */
    public void moveCursor(CursorDirection direction, int units) {
        if (direction == CursorDirection.UP) {
            if (cursorRow >= screenHeight - units) {
                throw new IllegalArgumentException("Cannot move cursor out of bounds");
            }

            cursorRow += units;
        }

        if (direction == CursorDirection.DOWN) {
            if (cursorRow < units) {
                throw new IllegalArgumentException("Cannot move cursor out of bounds");
            }

            cursorRow -= units;
        }

        if (direction == CursorDirection.LEFT) {
            if (cursorCol < units) {
                throw new IllegalArgumentException("Cannot move cursor out of bounds");
            }

            cursorCol -= units;
        }

        if (direction == CursorDirection.RIGHT) {
            if (cursorCol >= screenWidth - units) {
                throw new IllegalArgumentException("Cannot move cursor out of bounds");
            }

            cursorCol += units;
        }
    }

    public TerminalColor getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(TerminalColor foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public TerminalColor getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(TerminalColor backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    /**
     * Writes the given text at cursor position with selected attributes
     *
     * @param text text to write
     */
    public void writeText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char rawCharacter = text.charAt(i);
            CharacterCell formattedCharacter = new CharacterCell.Builder()
                    .content(rawCharacter)
                    .foregroundColor(foregroundColor)
                    .backgroundColor(backgroundColor)
                    .bold(bold)
                    .italic(italic)
                    .underline(underline)
                    .build();
            lines.get(cursorRow)[cursorCol] = formattedCharacter;
            cursorCol++;
            // wrap, if necessary
            if (cursorCol == screenWidth) {
                // we're indexing from bottom left, so going down is -1
                cursorRow--;
                cursorCol = 0;

                // if we're wrapping from the last line, create a new one
                if (cursorRow == -1) {
                    cursorRow = 0;
                    insertLine();
                }
            }
        }
    }

    /**
     * Inserts the given text at cursor position with selected attributes, pushing the following text to the right.
     * Note: This is a lossy operation, characters pushed to the right get discarded.
     *
     * @param text text to write
     */
    public void insertText(String text) {
        for (int i = 0; i < text.length(); i++) {
            char rawCharacter = text.charAt(i);
            CharacterCell formattedCharacter = new CharacterCell.Builder()
                    .content(rawCharacter)
                    .foregroundColor(foregroundColor)
                    .backgroundColor(backgroundColor)
                    .bold(bold)
                    .italic(italic)
                    .underline(underline)
                    .build();

            // this is pretty inefficient, as we're moving the whole line every character.
            // in a real application we could just override the characters until the last line and then
            // move them once by the desired amount.
            for (int j = screenWidth - 2; j >= cursorCol; j--) {
                lines.get(cursorRow)[j + 1] = lines.get(cursorRow)[j];
            }
            lines.get(cursorRow)[cursorCol] = formattedCharacter;

            cursorCol++;
            // wrap, if necessary
            if (cursorCol == screenWidth) {
                // we're indexing from bottom left, so going down is -1
                cursorRow--;
                cursorCol = 0;

                // if we're wrapping from the last line, create a new one
                if (cursorRow == -1) {
                    cursorRow = 0;
                    insertLine();
                }
            }
        }
    }

    /**
     * Clears the line which the cursor is on
     */
    public void clearLine() {
        Arrays.fill(lines.get(cursorRow), new CharacterCell());
    }

    /**
     * Fills the line which the cursor is on with a character
     *
     * @param character character to fill with
     */
    public void fillLine(CharacterCell character) {
        Arrays.fill(lines.get(cursorRow), character);
    }

    /**
     * Fills the whole screen with a character
     *
     * @param character character to fill with
     */
    public void fillScreen(CharacterCell character) {
        for (int i = 0; i < screenHeight; i++) {
            CharacterCell[] emptyLine = new CharacterCell[screenWidth];
            Arrays.fill(emptyLine, character);
            lines.addFirst(emptyLine);
            lines.removeLast();
        }
    }

    /**
     * Inserts a new line at the bottom of the screen
     */
    public void insertLine() {
        CharacterCell[] emptyLine = new CharacterCell[screenWidth];
        Arrays.fill(emptyLine, new CharacterCell());
        lines.addFirst(emptyLine);
        lines.removeLast();
    }

    /**
     * Clears the whole screen and pushes it to scrollback
     */
    public void clearScreen() {
        for (int i = 0; i < screenHeight; i++) {
            insertLine();
        }
    }

    /**
     * Clears the whole screen and scrollback
     */
    public void clearScreenAndScrollback() {
        for (int i = 0; i < screenHeight + scrollbackHeight; i++) {
            insertLine();
        }
    }

    /**
     * Gets the character, or empty character, at a position on the screen or scrollback
     *
     * @param row row
     * @param col column
     * @return character if present, empty otherwise
     * @throws IllegalArgumentException if coordinates are out of bounds
     */
    public Optional<Character> getRawCharacterAtPosition(int row, int col) {
        if (row < 0 || row >= screenHeight + scrollbackHeight ||
                col < 0 || col >= screenWidth) {
            throw new IllegalArgumentException("Cannot get character out of bounds");
        }

        return lines.get(row)[col].getRawCharacter();
    }

    /**
     * Gets the formatted character, with attributes, at a position on the screen or scrollback
     *
     * @param row row
     * @param col column
     * @return formatted character
     * @throws IllegalArgumentException if coordinates are out of bounds
     */
    public CharacterCell getCharacterAtPosition(int row, int col) {
        if (row < 0 || row >= screenHeight + scrollbackHeight ||
                col < 0 || col >= screenWidth) {
            throw new IllegalArgumentException("Cannot get character out of bounds");
        }

        return lines.get(row)[col];
    }

    /**
     * Gets a line from the screen or scrollback as a formatted string
     *
     * @param row row
     * @return formatted string
     * @throws IllegalArgumentException if row is out of bounds
     */
    public String getLine(int row) {
        if (row < 0 || row >= screenHeight + scrollbackHeight) {
            throw new IllegalArgumentException("Cannot get character out of bounds");
        }

        return Arrays.stream(lines.get(row)).map(CharacterCell::toString)
                .collect(Collectors.joining(""));
    }

    /**
     * Gets the entire visible screen as a formatted string
     *
     * @return formatted string
     */
    public String getScreen() {
        return IntStream.iterate(screenHeight - 1, i -> i >= 0, i -> i - 1)
                .mapToObj(this::getLine)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Gets the entire visible screen and scrollback as a formatted string
     *
     * @return formatted string
     */
    public String getScreenAndScrollback() {
        return IntStream.iterate(screenHeight + scrollbackHeight - 1, i -> i >= 0, i -> i - 1)
                .mapToObj(this::getLine)
                .collect(Collectors.joining("\n"));
    }
}
