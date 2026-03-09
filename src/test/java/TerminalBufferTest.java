import com.simondmc.CharacterCell;
import com.simondmc.CursorDirection;
import com.simondmc.TerminalBuffer;
import com.simondmc.TerminalColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalBufferTest {

    private final int SCREEN_WIDTH = 10;
    private final int SCREEN_HEIGHT = 5;
    private final int SCROLLBACK_HEIGHT = 5;
    private TerminalBuffer buffer;

    @BeforeEach
    public void setUp() {
        buffer = new TerminalBuffer(SCREEN_WIDTH, SCREEN_HEIGHT, SCROLLBACK_HEIGHT);
    }

    @Test
    public void testInitialization() {
        assertEquals(0, buffer.getCursorRow());
        assertEquals(0, buffer.getCursorCol());
        // Verify screen and scrollback are filled with empty lines (no exceptions thrown)
        assertDoesNotThrow(() -> buffer.getLine(0));
        assertDoesNotThrow(() -> buffer.getLine(SCREEN_HEIGHT + SCROLLBACK_HEIGHT - 1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getLine(SCREEN_HEIGHT + SCROLLBACK_HEIGHT));
    }

    @Test
    public void testCursorRowGetAndSet() {
        buffer.setCursorRow(3);
        assertEquals(3, buffer.getCursorRow());

        assertThrows(IllegalArgumentException.class, () -> buffer.setCursorRow(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.setCursorRow(SCREEN_HEIGHT));
    }

    @Test
    public void testCursorColGetAndSet() {
        buffer.setCursorCol(5);
        assertEquals(5, buffer.getCursorCol());

        assertThrows(IllegalArgumentException.class, () -> buffer.setCursorCol(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.setCursorCol(SCREEN_WIDTH));
    }

    @Test
    public void testMoveCursorUp() {
        buffer.setCursorRow(0);
        buffer.moveCursor(CursorDirection.UP, 3);
        assertEquals(3, buffer.getCursorRow());

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursor(CursorDirection.UP, SCREEN_HEIGHT));
    }

    @Test
    public void testMoveCursorDown() {
        buffer.setCursorRow(4);
        buffer.moveCursor(CursorDirection.DOWN, 2);
        assertEquals(2, buffer.getCursorRow());

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursor(CursorDirection.DOWN, 5));
    }

    @Test
    public void testMoveCursorLeft() {
        buffer.setCursorCol(4);
        buffer.moveCursor(CursorDirection.LEFT, 2);
        assertEquals(2, buffer.getCursorCol());

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursor(CursorDirection.LEFT, 5));
    }

    @Test
    public void testMoveCursorRight() {
        buffer.setCursorCol(0);
        buffer.moveCursor(CursorDirection.RIGHT, 4);
        assertEquals(4, buffer.getCursorCol());

        assertThrows(IllegalArgumentException.class, () -> buffer.moveCursor(CursorDirection.RIGHT, SCREEN_WIDTH));
    }

    @Test
    public void testStylingSettersAndWriteText() {
        buffer.setForegroundColor(TerminalColor.RED);
        buffer.setBackgroundColor(TerminalColor.BLUE);
        buffer.setBold(true);
        buffer.setItalic(true);
        buffer.setUnderline(true);

        buffer.writeText("A");

        // Assert styling applied to the written character
        CharacterCell cell = buffer.getCharacterAtPosition(0, 0);
        assertEquals(Optional.of('A'), cell.getRawCharacter());
        assertEquals(TerminalColor.RED, cell.getForegroundColor());
        assertEquals(TerminalColor.BLUE, cell.getBackgroundColor());
        assertTrue(cell.isBold());
        assertTrue(cell.isItalic());
        assertTrue(cell.isUnderline());

        assertEquals(1, buffer.getCursorCol()); // Cursor advanced
    }

    @Test
    public void testWriteTextWrappingAndLineInsertion() {
        buffer.setCursorRow(0);
        buffer.setCursorCol(SCREEN_WIDTH - 2);

        buffer.writeText("ABC"); // 2 fit on current line, 1 wraps

        assertEquals(1, buffer.getCursorCol());
        assertEquals(0, buffer.getCursorRow()); // Wrapped back to 0 because we were at row 0

        Optional<Character> wrappedChar = buffer.getRawCharacterAtPosition(0, 0);
        assertEquals(Optional.of('C'), wrappedChar);
    }

    @Test
    public void testInsertTextShiftsCharacters() {
        buffer.writeText("AC");
        buffer.setCursorCol(1);

        buffer.insertText("B");

        assertEquals(Optional.of('A'), buffer.getRawCharacterAtPosition(0, 0));
        assertEquals(Optional.of('B'), buffer.getRawCharacterAtPosition(0, 1));
        assertEquals(Optional.of('C'), buffer.getRawCharacterAtPosition(0, 2));
        assertEquals(2, buffer.getCursorCol());
    }

    @Test
    public void testClearLine() {
        buffer.writeText("TEST");
        buffer.clearLine();

        // Ensure character is empty
        CharacterCell cell = buffer.getCharacterAtPosition(0, 0);
        assertEquals(Optional.empty(), cell.getRawCharacter());
    }

    @Test
    public void testFillLine() {
        CharacterCell fillChar = new CharacterCell.Builder().content('#').build();
        buffer.fillLine(fillChar);

        for (int i = 0; i < SCREEN_WIDTH; i++) {
            assertEquals(Optional.of('#'), buffer.getRawCharacterAtPosition(0, i));
        }
    }

    @Test
    public void testFillScreen() {
        CharacterCell fillChar = new CharacterCell.Builder().content('*').build();
        buffer.fillScreen(fillChar);

        for (int row = 0; row < SCREEN_HEIGHT; row++) {
            assertEquals(Optional.of('*'), buffer.getRawCharacterAtPosition(row, 0));
        }
    }

    @Test
    public void testInsertLine() {
        buffer.writeText("L1");
        buffer.insertLine();

        // The written text should now be pushed up to row 1
        assertEquals(Optional.of('L'), buffer.getRawCharacterAtPosition(1, 0));
        assertEquals(Optional.of('1'), buffer.getRawCharacterAtPosition(1, 1));
        // Row 0 should be empty
        assertEquals(Optional.empty(), buffer.getRawCharacterAtPosition(0, 0));
    }

    @Test
    public void testClearScreen() {
        buffer.fillScreen(new CharacterCell.Builder().content('X').build());
        buffer.clearScreen();

        // Screen should be empty, but scrollback should have the 'X's
        assertEquals(Optional.empty(), buffer.getRawCharacterAtPosition(0, 0));
        assertEquals(Optional.of('X'), buffer.getRawCharacterAtPosition(SCREEN_HEIGHT, 0));
    }

    @Test
    public void testClearScreenAndScrollback() {
        buffer.fillScreen(new CharacterCell.Builder().content('Y').build());
        buffer.clearScreenAndScrollback();

        // Everything should be empty
        assertEquals(Optional.empty(), buffer.getRawCharacterAtPosition(0, 0));
        assertEquals(Optional.empty(), buffer.getRawCharacterAtPosition(SCREEN_HEIGHT, 0));
    }

    @Test
    public void testGetRawCharacterAtPositionOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> buffer.getRawCharacterAtPosition(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getRawCharacterAtPosition(0, -1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getRawCharacterAtPosition(SCREEN_HEIGHT + SCROLLBACK_HEIGHT, 0));
    }

    @Test
    public void testGetCharacterAtPositionOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> buffer.getCharacterAtPosition(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> buffer.getCharacterAtPosition(0, SCREEN_WIDTH));
    }

    @Test
    public void testGetLine() {
        buffer.writeText("HI");
        String line = buffer.getLine(0);
        assertNotNull(line);
        assertFalse(line.isEmpty());
    }

    @Test
    public void testGetLineOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> buffer.getLine(-1));
        assertThrows(IllegalArgumentException.class, () -> buffer.getLine(SCREEN_HEIGHT + SCROLLBACK_HEIGHT));
    }

    @Test
    public void testGetScreen() {
        String screen = buffer.getScreen();
        // Since it concatenates SCREEN_HEIGHT lines with '\n', it should have SCREEN_HEIGHT - 1 newlines
        assertEquals(SCREEN_HEIGHT - 1, screen.chars().filter(ch -> ch == '\n').count());
    }

    @Test
    public void testGetScreenAndScrollback() {
        String fullBuffer = buffer.getScreenAndScrollback();
        int totalLines = SCREEN_HEIGHT + SCROLLBACK_HEIGHT;
        assertEquals(totalLines - 1, fullBuffer.chars().filter(ch -> ch == '\n').count());
    }
}