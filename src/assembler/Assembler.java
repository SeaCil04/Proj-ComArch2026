package assembler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for the two-pass assembler.
 *
 * Reads LC-2200 assembly source code, builds the symbol table (Pass 1),
 * encodes instructions into 32-bit machine code (Pass 2), and writes the output file.
 */
public class Assembler {

    private static final int MAX_MEMORY = 65536;

    private final SymbolTable symbolTable = new SymbolTable();
    private final List<Parser.InstructionLine> program = new ArrayList<>();

    /**
     * Main method to execute the assembler from command line arguments.
     *
     * @param args command-line arguments: [0] input file, [1] output file
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                    "Usage: java Assembler <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }

        try {
            Assembler assembler = new Assembler();
            assembler.assemble(args[0], args[1]);
            System.exit(0);
        } catch (AssemblerException e) {
            System.err.println("Assembler error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Runs the full two-pass assembly process on the specified source file.
     *
     * @param inputFile  path to the assembly source file
     * @param outputFile path to write the generated machine code
     * @throws IOException        if an I/O error occurs reading or writing files
     * @throws AssemblerException if a syntax, parsing, or encoding error occurs
     */
    public void assemble(String inputFile, String outputFile)
            throws IOException {

        readProgram(inputFile);
        pass1();
        pass2(outputFile);
    }

    // Reads and parses lines from the source file into instruction objects
    private void readProgram(String inputFile) throws IOException {
        program.clear();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(inputFile))) {

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {

                int address = program.size();

                Parser.InstructionLine parsed =
                        Parser.parse(line, lineNumber,address);

                if (parsed != null) {
                    program.add(parsed);
                }

                lineNumber++;
            }
        }

        if (program.size() > MAX_MEMORY) {
            throw new AssemblerException(
                    "Program is larger than 65536 words");
        }
    }

    // Pass 1: Scans program lines and builds the symbol table mapping labels to addresses
    private void pass1() {
        symbolTable.clear();

        for (int address = 0; address < program.size(); address++) {
            Parser.InstructionLine line = program.get(address);

            if (line.getLabel() != null) {
                symbolTable.add(line.getLabel(), address);
            }
        }
    }

    // Pass 2: Encodes each instruction into machine code and writes to output file
    private void pass2(String outputFile) throws IOException {
        Encoder encoder = new Encoder();
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (Parser.InstructionLine line : program) {
                int machineCode = encoder.encode(line, symbolTable);
                writer.println(machineCode);
            }
        }
    }

}
