package components;

public class AutomatonCrashException extends RuntimeException {
    String rejectedString;

    public AutomatonCrashException(String message, String rejectedString) {
        super(message);
        this.rejectedString = rejectedString;
    }
}
