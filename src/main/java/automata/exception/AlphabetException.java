package automata.exception;

/**
 * General exception thrown when an Alphabet, or interactions with it, result
 * in undefined or invalid behavior in an Automaton. <br>
 * Examples:
 * <ul>
 *     <li>An Alphabet with an invalid number of symbols</li>
 *     <li>A string that does not conform to the alphabet that a DFA uses</li>
 *     <li>Performing operations on DFAs with incompatible alphabets</li>
 * </ul>
 */
public class AlphabetException extends RuntimeException {
    public AlphabetException(String message) {
        super(message);
    }
}
