package assembler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Assembler {

    private static final int MAX_MEMORY = 65536;

    private final SymbolTable symbolTable = new SymbolTable();
    private final List<Parser.InstructionLine> program = new ArrayList<>();

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

    public void assemble(String inputFile, String outputFile)
            throws IOException {

        readProgram(inputFile);
        pass1();
        pass2(outputFile);
    }

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

    private void pass1() {
        symbolTable.clear();

        for (int address = 0; address < program.size(); address++) {
            Parser.InstructionLine line = program.get(address);

            if (line.getLabel() != null) {
                symbolTable.add(line.getLabel(), address);
            }
        }
    }

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
