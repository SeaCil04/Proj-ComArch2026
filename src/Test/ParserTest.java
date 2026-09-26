package Test;

import assembler.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    // ---------------------------------------------------------------
    // Blank / comment-only lines
    // ---------------------------------------------------------------

    @Test
    void blankLine_returnsNull() {
        assertNull(Parser.parse("", 1, 0));
        assertNull(Parser.parse("   ", 1, 0));
        assertNull(Parser.parse("\t\t", 1, 0));
    }

    // ---------------------------------------------------------------
    // Position-based label detection (the bug we fixed)
    // ---------------------------------------------------------------

    @Test
    void noLeadingWhitespace_firstTokenIsLabel() {
        Parser.InstructionLine line = Parser.parse("start add 1 2 3", 1, 0);
        assertEquals("start", line.getLabel());
        assertEquals("add", line.getOpcode());
        assertArrayEquals(new String[]{"1", "2", "3"}, line.getArgs());
    }

    @Test
    void leadingWhitespace_noLabel() {
        Parser.InstructionLine line = Parser.parse("    add 1 2 3", 1, 0);
        assertNull(line.getLabel());
        assertEquals("add", line.getOpcode());
    }

    @Test
    void leadingTab_noLabel() {
        Parser.InstructionLine line = Parser.parse("\tlw 0 1 five", 1, 0);
        assertNull(line.getLabel());
        assertEquals("lw", line.getOpcode());
    }

    /**
     * The exact bug this fix addresses: a label whose NAME happens to match
     * an opcode must still be read as a label if the line has no leading
     * whitespace, never mistaken for the opcode itself.
     */
    @Test
    void labelNamedLikeOpcode_stillParsedAsLabel() {
        Parser.InstructionLine line = Parser.parse("lw add 1 2 3", 1, 0);
        assertEquals("lw", line.getLabel());
        assertEquals("add", line.getOpcode());
        assertArrayEquals(new String[]{"1", "2", "3"}, line.getArgs());
    }

    @Test
    void noLabel_lineStartsWithSpace_evenIfFirstTokenNotAnOpcode() {
        // With leading whitespace, the first token is ALWAYS the opcode
        // slot; if it isn't a real opcode this must fail as invalid
        // opcode, not be silently reinterpreted as a label.
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse(" bogus 1 2 3", 1, 0));
        assertTrue(ex.getMessage().contains("invalid opcode"));
    }

    // ---------------------------------------------------------------
    // Address propagation
    // ---------------------------------------------------------------

    @Test
    void address_isStoredOnInstructionLine() {
        Parser.InstructionLine line = Parser.parse("start add 1 2 3", 5, 42);
        assertEquals(42, line.getAddress());
        assertEquals(5, line.getLineNumber());
    }

    // ---------------------------------------------------------------
    // Argument count validation
    // ---------------------------------------------------------------

    @Test
    void rType_missingArgument_throws() {
        assertThrows(AssemblerException.class,
                () -> Parser.parse("add 1 2", 1, 0));
    }

    @Test
    void jalr_missingArgument_throws() {
        assertThrows(AssemblerException.class,
                () -> Parser.parse("jalr 1", 1, 0));
    }

    @Test
    void halt_extraTokensIgnoredAsComment() {
        // Per spec, unused trailing fields are treated as comments.
        Parser.InstructionLine line =
                Parser.parse("done halt end of program", 1, 0);
        assertEquals("halt", line.getOpcode());
    }

    @Test
    void fill_requiresOneArgument() {
        assertThrows(AssemblerException.class,
                () -> Parser.parse("five .fill", 1, 0));
    }

    @Test
    void fill_withNumericArgument_parsesOk() {
        Parser.InstructionLine line = Parser.parse("five .fill 5", 1, 0);
        assertEquals(".fill", line.getOpcode());
        assertEquals("5", line.getArg(0));
    }

    // ---------------------------------------------------------------
    // Invalid opcode
    // ---------------------------------------------------------------

    @Test
    void unknownOpcode_throws() {
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse("start foo 1 2 3", 1, 0));
        assertTrue(ex.getMessage().contains("invalid opcode"));
    }

    // ---------------------------------------------------------------
    // Label validation
    // ---------------------------------------------------------------

    @Test
    void label_tooLong_throws() {
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse("toolonglabel add 1 2 3", 1, 0));
        assertTrue(ex.getMessage().contains("longer than 6"));
    }

    @Test
    void label_startsWithDigit_throws() {
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse("1abc add 1 2 3", 1, 0));
        assertTrue(ex.getMessage().contains("must start with a letter"));
    }

    @Test
    void label_containsInvalidCharacter_throws() {
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse("ab_cd add 1 2 3", 1, 0));
        assertTrue(ex.getMessage().contains("invalid label"));
    }

    @Test
    void label_exactlySixCharacters_ok() {
        Parser.InstructionLine line = Parser.parse("abcdef add 1 2 3", 1, 0);
        assertEquals("abcdef", line.getLabel());
    }

    @Test
    void label_alphanumeric_ok() {
        Parser.InstructionLine line = Parser.parse("a1b2 add 1 2 3", 1, 0);
        assertEquals("a1b2", line.getLabel());
    }

    @Test
    void noLabel_missingOpcodeAfterLabel_throws() {
        // A line with only a label token and nothing after it.
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> Parser.parse("labels", 1, 0));
        assertTrue(ex.getMessage().contains("missing opcode"));
    }


    // ---------------------------------------------------------------
    // isOpcode / isNumber helpers
    // ---------------------------------------------------------------

    @Test
    void isOpcode_recognizesAllOpcodesCaseInsensitive() {
        assertTrue(Parser.isOpcode("add"));
        assertTrue(Parser.isOpcode("ADD"));
        assertTrue(Parser.isOpcode(".fill"));
        assertFalse(Parser.isOpcode("mul"));
        assertFalse(Parser.isOpcode(null));
    }

    @Test
    void isNumber_detectsIntegersOnly() {
        assertTrue(Parser.isNumber("123"));
        assertTrue(Parser.isNumber("-45"));
        assertFalse(Parser.isNumber("abc"));
        assertFalse(Parser.isNumber(""));
        assertFalse(Parser.isNumber(null));
    }

    // ---------------------------------------------------------------
    // Null line
    // ---------------------------------------------------------------

    @Test
    void nullLine_throws() {
        assertThrows(AssemblerException.class,
                () -> Parser.parse(null, 1, 0));
    }
}
