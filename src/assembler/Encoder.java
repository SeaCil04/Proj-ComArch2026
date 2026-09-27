package assembler;

import java.util.HashMap;
import java.util.Map;

import assembler.Parser.InstructionLine;

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