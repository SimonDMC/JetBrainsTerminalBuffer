package com.simondmc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterCell {
    private static final int BOLD_TERMINAL_CODE = 1;
    private static final int ITALIC_TERMINAL_CODE = 3;
    private static final int UNDERLINE_TERMINAL_CODE = 4;

    private final char content;
    private final TerminalColor foregroundColor;
    private final TerminalColor backgroundColor;
    private final boolean bold;
    private final boolean italic;
    private final boolean underline;

    public CharacterCell() {
        this(new CharacterCell.Builder());
    }

    public CharacterCell(Builder builder) {
        this.content = builder.content;
        this.foregroundColor = builder.foregroundColor;
        this.backgroundColor = builder.backgroundColor;
        this.bold = builder.bold;
        this.italic = builder.italic;
        this.underline = builder.underline;
    }

    /**
     * Get character cell as a formatted string
     *
     * @return formatted string
     */
    public String toString() {
        List<Integer> modifiers = new ArrayList<>();

        if (foregroundColor != TerminalColor.NONE) {
            modifiers.add(foregroundColor.foregroundColor());
        }

        if (backgroundColor != TerminalColor.NONE) {
            modifiers.add(backgroundColor.backgroundColor());
        }

        if (bold) {
            modifiers.add(BOLD_TERMINAL_CODE);
        }

        if (italic) {
            modifiers.add(ITALIC_TERMINAL_CODE);
        }

        if (underline) {
            modifiers.add(UNDERLINE_TERMINAL_CODE);
        }

        if (modifiers.isEmpty()) {
            // if there are no modifiers, return character directly
            return String.valueOf(content);
        } else {
            // if there are modifiers, wrap the character in formatting code and reset
            String joinedModifiers = modifiers.stream().map(Object::toString)
                    .collect(Collectors.joining(";"));
            String formattingCode = "\u001B[" + joinedModifiers + "m";
            String resetCode = "\u001B[0m";

            return formattingCode + content + resetCode;
        }
    }

    public static class Builder {
        private char content = ' ';
        private TerminalColor foregroundColor = TerminalColor.NONE;
        private TerminalColor backgroundColor = TerminalColor.NONE;
        private boolean bold = false;
        private boolean italic = false;
        private boolean underline = false;

        public Builder content(Character content) {
            this.content = content;
            return this;
        }

        public Builder foregroundColor(TerminalColor foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        public Builder backgroundColor(TerminalColor backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder bold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public Builder italic(boolean italic) {
            this.italic = italic;
            return this;
        }

        public Builder underline(boolean underline) {
            this.underline = underline;
            return this;
        }

        public CharacterCell build() {
            return new CharacterCell(this);
        }
    }
}
