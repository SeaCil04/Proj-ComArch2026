package assembler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Parser {
    private static final Set<String> OPCODES = new HashSet<>(
            Arrays.asList("add", "nand", "lw", "sw", "beq",
                    "jalr", "halt", "noop", ".fill")
    );

    /**
     * @param line       raw line from the assembly source file
     * @param lineNumber 1-based line number, used for error messages
     * @param address    memory address this line will occupy (word index,
     *                   starting at 0, counting .fill lines too). Caller is
     *                   responsible for only incrementing this for lines
     *                   that are not blank/comment-only (i.e. lines for
     *                   which this method does not return null).
     */
    public static InstructionLine parse(String line, int lineNumber, int address) {
        if (line == null) {
            throw new AssemblerException("Line " + lineNumber + ": null line");
        }

        // Position matters BEFORE we trim: whether the line starts with
        // whitespace tells us whether a label is present. Capture that
        // first, since trim() would destroy this information.
        boolean hasLeadingWhitespace = !line.isEmpty()
                && (line.charAt(0) == ' ' || line.charAt(0) == '\t');

        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        String[] tokens = trimmed.split("\\s+");

        int index = 0;
        String label = null;

        // The FIRST token is a label if and only if the original line did
        // not start with whitespace. We no longer guess based on whether
        // the token looks like an opcode -- a label named "lw" or "add"
        // is legal and must not be misread as an opcode.
        if (!hasLeadingWhitespace) {
            label = tokens[index++];
            validateLabel(label, lineNumber);

            if (index >= tokens.length) {
                throw new AssemblerException(
                        "Line " + lineNumber + ": missing opcode");
            }
        }

        String opcode = tokens[index++].toLowerCase();

        if (!isOpcode(opcode)) {
            throw new AssemblerException(
                    "Line " + lineNumber + ": invalid opcode '" + opcode + "'");
        }

        String[] args = new String[tokens.length - index];
        System.arraycopy(tokens, index, args, 0, args.length);

        validateArgumentCount(opcode, args.length, lineNumber);

        return new InstructionLine(label, opcode, args, lineNumber, address);
    }

    private static void validateArgumentCount(
            String opcode, int count, int lineNumber) {

        int expected;

        switch (opcode) {
            case "add":
            case "nand":
            case "lw":
            case "sw":
            case "beq":
                expected = 3;
                break;

            case "jalr":
                expected = 2;
                break;

            case "halt":
            case "noop":
                expected = 0;
                break;

            case ".fill":
                expected = 1;
                break;

            default:
                throw new AssemblerException(
                        "Line " + lineNumber + ": invalid opcode '" + opcode + "'");
        }

        if (count < expected) {
            throw new AssemblerException(
                    "Line " + lineNumber + ": opcode '" + opcode
                            + "' expects " + expected + " argument(s), got " + count);
        }

        // Extra tokens are intentionally ignored because the assignment
        // defines unused fields as comments.
    }

    public static boolean isOpcode(String token) {
        return token != null && OPCODES.contains(token.toLowerCase());
    }

    public static boolean isNumber(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void validateLabel(String label, int lineNumber) {
        if (label.length() > 6) {
            throw new AssemblerException(
                    "Line " + lineNumber + ": label '" + label
                            + "' is longer than 6 characters");
        }

        if (!Character.isLetter(label.charAt(0))) {
            throw new AssemblerException(
                    "Line " + lineNumber + ": label '" + label
                            + "' must start with a letter");
        }

        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);

            if (!Character.isLetterOrDigit(c)) {
                throw new AssemblerException(
                        "Line " + lineNumber + ": invalid label '" + label + "'");
            }
        }
    }

    /**
     * Parsed representation of one assembly source line.
     */
    public static class InstructionLine {
        private final String label;
        private final String opcode;
        private final String[] args;
        private final int lineNumber;
        private final int address;

        public InstructionLine(
                String label,
                String opcode,
                String[] args,
                int lineNumber,
                int address) {

            this.label = label;
            this.opcode = opcode;
            this.args = args;
            this.lineNumber = lineNumber;
            this.address = address;
        }

        public String getLabel() {
            return label;
        }

        public String getOpcode() {
            return opcode;
        }

        public String[] getArgs() {
            return args;
        }

        public String getArg(int index) {
            return args[index];
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getAddress() {
            return address;
        }
    }
}
