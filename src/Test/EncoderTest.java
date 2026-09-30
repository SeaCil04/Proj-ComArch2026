package Test;

import assembler.*;
import assembler.Parser.InstructionLine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EncoderTest {

    private final Encoder encoder = new Encoder();

    // ---------------------------------------------------------------
    // R-type / J-type / O-type - one representative test each, since
    // the encoding logic is identical across opcodes in the same format.
    // ---------------------------------------------------------------

    @Test
    void add_encodesCorrectly() {
        InstructionLine ins = new InstructionLine(
                null, "add", new String[]{"1", "2", "3"}, 1, 0);
        // opcode=0, regA=1, regB=2, destReg=3
        int expected = (0 << 22) | (1 << 19) | (2 << 16) | 3;
        assertEquals(expected, encoder.encode(ins, new SymbolTable()));
    }

    @Test
    void jalr_encodesCorrectly() {
        InstructionLine ins = new InstructionLine(
                null, "jalr", new String[]{"2", "4"}, 1, 0);
        int expected = (5 << 22) | (2 << 19) | (4 << 16);
        assertEquals(expected, encoder.encode(ins, new SymbolTable()));
    }


    @Test
    void halt_encodesCorrectly() {
        InstructionLine ins = new InstructionLine(
                "done", "halt", new String[]{}, 1, 6);
        assertEquals(6 << 22, encoder.encode(ins, new SymbolTable()));
    }

    // ---------------------------------------------------------------
    // I-type: the two things that actually need separate coverage -
    // numeric offset, and label offset where lw/sw (absolute) and
    // beq (relative to pc+1) must NOT use the same formula.
    // ---------------------------------------------------------------

    @Test
    void lw_numericOffset_encodesCorrectly() {
        InstructionLine ins = new InstructionLine(
                null, "lw", new String[]{"0", "1", "3"}, 1, 5);
        int expected = (2 << 22) | (0 << 19) | (1 << 16) | 3;
        assertEquals(expected, encoder.encode(ins, new SymbolTable()));
    }

    @Test
    void lw_labelOffset_usesAbsoluteAddress() {
        SymbolTable symTab = new SymbolTable();
        symTab.add("five", 7);
        InstructionLine ins = new InstructionLine(
                null, "lw", new String[]{"0", "1", "five"}, 1, 0);
        int expected = (2 << 22) | (0 << 19) | (1 << 16) | 7;
        assertEquals(expected, encoder.encode(ins, symTab));
    }

    @Test
    void beq_labelOffset_usesAddressRelativeToPcPlusOne() {
        SymbolTable symTab = new SymbolTable();
        symTab.add("start", 2);
        // beq is at address 4, so offset = 2 - (4 + 1) = -3 (negative case)
        InstructionLine ins = new InstructionLine(
                null, "beq", new String[]{"0", "0", "start"}, 1, 4);
        int expected = (4 << 22) | (0 << 19) | (0 << 16) | (-3 & 0xFFFF);
        assertEquals(expected, encoder.encode(ins, symTab));
    }

    // ---------------------------------------------------------------
    // .fill - both forms (number and label) since they're genuinely
    // different code paths and easy to forget entirely.
    // ---------------------------------------------------------------

    @Test
    void fill_withNumericArgument_encodesTheNumberItself() {
        InstructionLine ins = new InstructionLine(
                "neg1", ".fill", new String[]{"-1"}, 1, 8);
        assertEquals(-1, encoder.encode(ins, new SymbolTable()));
    }

    @Test
    void fill_withLabelArgument_encodesTheLabelsAddress() {
        SymbolTable symTab = new SymbolTable();
        symTab.add("start", 2);
        InstructionLine ins = new InstructionLine(
                "stAddr", ".fill", new String[]{"start"}, 1, 9);
        assertEquals(2, encoder.encode(ins, symTab));
    }

    // ---------------------------------------------------------------
    // Required errors: offset out of range (Encoder's own) and
    // undefined label (thrown by SymbolTable, must propagate through).
    // ---------------------------------------------------------------

    @Test
    void offset_tooLarge_throws() {
        InstructionLine ins = new InstructionLine(
                null, "lw", new String[]{"0", "1", "32768"}, 1, 0);
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> encoder.encode(ins, new SymbolTable()));
        assertTrue(ex.getMessage().contains("out of range"));
    }

    @Test
    void undefinedLabel_throws() {
        InstructionLine ins = new InstructionLine(
                null, "lw", new String[]{"0", "1", "nosuch"}, 1, 0);
        AssemblerException ex = assertThrows(AssemblerException.class,
                () -> encoder.encode(ins, new SymbolTable()));
        assertTrue(ex.getMessage().contains("Undefined label"));
    }

    // ---------------------------------------------------------------
    // Full integration: the counting-down-from-5 example from the spec,
    // where the expected machine code is given - proves every format
    // works together correctly, all at once.
    // ---------------------------------------------------------------

    @Test
    void countingExample_matchesSpecExactly() {
        InstructionLine[] program = {
                new InstructionLine(null,     "lw",   new String[]{"0","1","five"},  1, 0),
                new InstructionLine(null,     "lw",   new String[]{"1","2","3"},     2, 1),
                new InstructionLine("start",  "add",  new String[]{"1","2","1"},     3, 2),
                new InstructionLine(null,     "beq",  new String[]{"0","1","2"},     4, 3),
                new InstructionLine(null,     "beq",  new String[]{"0","0","start"}, 5, 4),
                new InstructionLine(null,     "noop", new String[]{},                6, 5),
                new InstructionLine("done",   "halt", new String[]{},                7, 6),
                new InstructionLine("five",   ".fill", new String[]{"5"},            8, 7),
                new InstructionLine("neg1",   ".fill", new String[]{"-1"},           9, 8),
                new InstructionLine("stAddr", ".fill", new String[]{"start"},       10, 9),
        };
        int[] expected = {
                8454151, 9043971, 655361, 16842754, 16842749,
                29360128, 25165824, 5, -1, 2
        };

        SymbolTable symTab = new SymbolTable();
        for (InstructionLine ins : program) {
            if (ins.getLabel() != null) {
                symTab.add(ins.getLabel(), ins.getAddress());
            }
        }

        for (int i = 0; i < program.length; i++) {
            assertEquals(expected[i], encoder.encode(program[i], symTab),
                    "mismatch at address " + i);
        }
    }
}