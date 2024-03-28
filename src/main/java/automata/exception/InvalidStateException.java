package automata.exception;

/**
 * General exception used when an invalid state is detected in an Automaton.
 * Usually occurs when combining multiple automata that happen to reference
 * the same state in different ways.
 */
public class InvalidStateException extends RuntimeException {
    public InvalidStateException(String msg) {
        super(msg);
    }
}
