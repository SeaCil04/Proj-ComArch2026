package assembler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Integer> table = new HashMap<>();

    /**
     * Add a label.
     *
     * @throws AssemblerException if the label already exists
     */
    public void add(String label, int address) {
        if (table.containsKey(label)) {
            throw new AssemblerException(
                    "Duplicate label: '" + label + "'");
        }

        table.put(label, address);
    }

    public boolean contains(String label) {
        return table.containsKey(label);
    }

    /**
     * Get address of a label.
     *
     * @throws AssemblerException if the label is undefined
     */
    public int getAddress(String label) {
        Integer address = table.get(label);

        if (address == null) {
            throw new AssemblerException(
                    "Undefined label: '" + label + "'");
        }

        return address;
    }

    public int size() {
        return table.size();
    }

    public void clear() {
        table.clear();
    }

    @Override
    public String toString() {
        return table.toString();
    }
}
