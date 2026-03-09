import com.simondmc.CharacterCell;
import com.simondmc.TerminalColor;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterCellTest {

    @Test
    public void defaultCharacterCellIsEmptyAndUnformatted() {
        CharacterCell cell = new CharacterCell();

        assertEquals(Optional.empty(), cell.getRawCharacter());
        assertEquals(TerminalColor.NONE, cell.getForegroundColor());
        assertEquals(TerminalColor.NONE, cell.getBackgroundColor());
        assertFalse(cell.isBold());
        assertFalse(cell.isItalic());
        assertFalse(cell.isUnderline());

        assertEquals(" ", cell.toString());
    }

    @Test
    public void builderSetsAllPropertiesCorrectly() {
        CharacterCell cell = new CharacterCell.Builder()
                .content('X')
                .foregroundColor(TerminalColor.RED)
                .backgroundColor(TerminalColor.GREEN)
                .bold(true)
                .italic(true)
                .underline(true)
                .build();

        assertTrue(cell.getRawCharacter().isPresent());
        assertEquals('X', cell.getRawCharacter().get());
        assertEquals(TerminalColor.RED, cell.getForegroundColor());
        assertEquals(TerminalColor.GREEN, cell.getBackgroundColor());
        assertTrue(cell.isBold());
        assertTrue(cell.isItalic());
        assertTrue(cell.isUnderline());
    }

    @Test
    public void toStringReturnsRawCharacterWhenNoModifiersPresent() {
        CharacterCell cell = new CharacterCell.Builder()
                .content('A')
                .build();

        assertEquals("A", cell.toString());
    }

    @Test
    public void toStringReturnsFormattedSpaceWhenNoContentButHasModifiers() {
        CharacterCell cell = new CharacterCell.Builder()
                .foregroundColor(TerminalColor.RED)
                .backgroundColor(TerminalColor.GREEN)
                .italic(true)
                .build();

        assertEquals("\u001B[31;42;3m \u001B[0m", cell.toString());
    }

    @Test
    public void toStringAppliesAllModifiersInCorrectOrder() {
        CharacterCell cell = new CharacterCell.Builder()
                .content('Z')
                .foregroundColor(TerminalColor.RED)   // 31
                .backgroundColor(TerminalColor.GREEN) // 42
                .bold(true)                           // 1
                .italic(true)                         // 3
                .underline(true)                      // 4
                .build();

        assertEquals("\u001B[31;42;1;3;4mZ\u001B[0m", cell.toString());
    }

    @Test
    public void toStringAppliesSingleModifierCorrectly() {
        CharacterCell cell = new CharacterCell.Builder()
                .content('B')
                .bold(true)
                .build();

        assertEquals("\u001B[1mB\u001B[0m", cell.toString());
    }
}