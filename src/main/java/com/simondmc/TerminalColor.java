package com.simondmc;

public enum TerminalColor {
    NONE,
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE;

    // Foreground terminal colors are codes 30 (for black) - 37 (for white)
    private static final int FOREGROUND_TERMINAL_OFFSET = 29;
    // Background terminal colors are codes 40 (for black) - 47 (for white)
    private static final int BACKGROUND_TERMINAL_OFFSET = 39;

    /**
     * Get the terminal code of a foreground terminal color
     *
     * @return terminal code, or 0 if unset
     */
    public int foregroundColor() {
        if (this == NONE) return 0;
        return this.ordinal() + FOREGROUND_TERMINAL_OFFSET;
    }

    /**
     * Get the terminal code of a background terminal color
     *
     * @return terminal code, or 0 if unset
     */
    public int backgroundColor() {
        if (this == NONE) return 0;
        return this.ordinal() + BACKGROUND_TERMINAL_OFFSET;
    }
}
