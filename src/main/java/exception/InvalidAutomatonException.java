package exception;

/**
 * General exception for when an invalid Automaton is attempted to be
 * instantiated or used. Usually indicates that a DFA is missing a state,
 * or an NFA is pointing to a state that is otherwise not in the set of states.
 */
public class InvalidAutomatonException extends RuntimeException {
    public InvalidAutomatonException(String msg) {
        super(msg);
    }
}
