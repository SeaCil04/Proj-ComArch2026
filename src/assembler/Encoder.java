package assembler;

import java.util.HashMap;
import java.util.Map;

import assembler.Parser.InstructionLine;

/**
 * Handles encoding of parsed instruction lines into machine code representation.
 */
public class Encoder {

    private static final Map<String, Integer> OPCODES = new HashMap<>();
    static {
        OPCODES.put("add", 0);
        OPCODES.put("nand", 1);
        OPCODES.put("lw", 2);
        OPCODES.put("sw", 3);
        OPCODES.put("beq", 4);
        OPCODES.put("jalr", 5);
        OPCODES.put("halt", 6);
        OPCODES.put("noop", 7);
    }

    /**
     * Encodes a parsed assembly instruction line into its 32-bit machine code.
     *
     * Dispatches the instruction to the appropriate encoding method based on its opcode.
     * Opcode validity is pre-validated during parsing.
     *
     * @param ins the parsed instruction line containing opcode, arguments, and line number
     * @param symTab the symbol table used to resolve label addresses
     * @return the 32-bit integer representing the encoded machine instruction or memory content
     * @throws AssemblerException if a register is invalid, or an offset/immediate is out of range
     */
    public int encode(InstructionLine ins, SymbolTable symTab) {
        switch (ins.getOpcode()) {
            case "add":
            case "nand":
                return encodeRType(ins);
            case "lw":
            case "sw":
            case "beq":
                return encodeIType(ins, symTab);
            case "jalr":
                return encodeJType(ins);
            case "halt":
            case "noop":
                return encodeOType(ins);
            case ".fill":
                return encodeFill(ins, symTab);
            default:
                // Should be unreachable - Parser already validates opcodes.
                throw new AssemblerException(
                        "Unhandled opcode '" + ins.getOpcode() + "' at line " + ins.getLineNumber());
        }
    }

    private int encodeRType(InstructionLine ins) {
        int opcode = OPCODES.get(ins.getOpcode());
        int regA = parseRegister(ins.getArg(0), ins);
        int regB = parseRegister(ins.getArg(1), ins);
        int destReg = parseRegister(ins.getArg(2), ins);
        return (opcode << 22) | (regA << 19) | (regB << 16) | destReg;
    }

    private int encodeIType(InstructionLine ins, SymbolTable symTab) {
        int opcode = OPCODES.get(ins.getOpcode());
        int regA = parseRegister(ins.getArg(0), ins);
        int regB = parseRegister(ins.getArg(1), ins);
        int offset = resolveOffset(ins, symTab);
        if (!isOffsetInRange(offset)) {
            throw new AssemblerException(
                    "Offset " + offset + " out of range (-32768 to 32767) at line " + ins.getLineNumber());
        }
        int offsetField = offset & 0xFFFF; // keep only the low 16 bits
        return (opcode << 22) | (regA << 19) | (regB << 16) | offsetField;
    }

    private int encodeJType(InstructionLine ins) {
        int opcode = OPCODES.get(ins.getOpcode());
        int regA = parseRegister(ins.getArg(0), ins);
        int regB = parseRegister(ins.getArg(1), ins);
        return (opcode << 22) | (regA << 19) | (regB << 16);
    }

    private int encodeOType(InstructionLine ins) {
        int opcode = OPCODES.get(ins.getOpcode());
        return opcode << 22;
    }

    private int encodeFill(InstructionLine ins, SymbolTable symTab) {
        String field = ins.getArg(0);
        if (Parser.isNumber(field)) {
            return Integer.parseInt(field);
        }
        // SymbolTable.getAddress() throws AssemblerException itself
        // if the label is undefined - nothing more to do here.
        return symTab.getAddress(field);
    }

    /**
     * Resolves the offset or target address for I-type instructions.
     *
     * Handles both immediate integer values and symbolic labels:
     * - lw / sw: Resolves labels to absolute memory addresses.
     * - beq: Resolves labels to PC-relative offsets computed as labelAddress - (currentPC + 1).
     *
     * @param ins the I-type instruction line
     * @param symTab the symbol table to look up defined labels
     * @return the calculated numeric offset or target address
     * @throws AssemblerException if the target label is undefined in the symbol table
     */
    private int resolveOffset(InstructionLine ins, SymbolTable symTab) {
        String field = ins.getArg(2);
        if (Parser.isNumber(field)) {
            return Integer.parseInt(field);
        }
        int labelAddress = symTab.getAddress(field); // throws if undefined
        if (ins.getOpcode().equals("beq")) {
            return labelAddress - (ins.getAddress() + 1);
        }
        return labelAddress;
    }

    public boolean isOffsetInRange(int offset) {
        return offset >= -32768 && offset <= 32767;
    }

    private int parseRegister(String field, InstructionLine ins) {
        if (!Parser.isNumber(field)) {
            throw new AssemblerException(
                    "Invalid register '" + field + "' at line " + ins.getLineNumber());
        }
        int reg = Integer.parseInt(field);
        if (reg < 0 || reg > 7) {
            throw new AssemblerException(
                    "Register " + reg + " out of range (0-7) at line " + ins.getLineNumber());
        }

        return reg;
    }
}