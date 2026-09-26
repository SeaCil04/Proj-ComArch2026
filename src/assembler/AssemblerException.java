package assembler;

public class AssemblerException extends RuntimeException {
    public AssemblerException(String message) {
        super(message);
    }
    public String getMessage() {
        return super.getMessage();
    }
}
