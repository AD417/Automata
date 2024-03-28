package automata.components;

import automata.exception.AlphabetException;

import java.util.HashSet;

/**
 * An Alphabet is defined as the set of all letters from which valid languages
 * can be constructed.
 */
public class Alphabet extends HashSet<Character> {
    /**
     * An unused control character representing Epsilon transitions in an NFA.
     * Epsilon transitions change an NFA's state without moving the tape head.
     */
    public static final Character EPSILON = 'ε'; // '\uFFFF';

    /**
     * An alphabet containing all uppercase and lowercase letters from a-z.
     */
    public static final Alphabet ALL_LETTERS = Alphabet.withSymbols(
            "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM"
    );

    /**
     * An alphabet containing all the symbols that can appear in valid numbers.
     */
    public static final Alphabet ALL_NUMBERS = Alphabet.withSymbols("1234567890-+.");

    /**
     * Create an alphabet containing the provided list of characters.
     * @param firstSymbol one of the symbols in the alphabet. An alphabet must
     *                    consist of at least one symbol.
     * @param otherSymbols the remaining symbols in the alphabet. An alphabet
     *                     may contain any number of unique symbols.
     * @return an Alphabet containing the given symbols.
     */
    public static Alphabet withSymbols(char firstSymbol, char ... otherSymbols) {
        Alphabet output = new Alphabet();
        output.add(firstSymbol);
        for (Character c : otherSymbols) output.add(c);

        return output;
    }

    /**
     * Create an alphabet containing the provided string of characters.
     * @param symbols a single string containing all the symbols, and only the
     *                symbols in the alphabet. Must contain at least 1 symbol.
     * @return an Alphabet containing the given symbols.
     */
    public static Alphabet withSymbols(String symbols) {
        if (symbols.isEmpty()) {
            throw new AlphabetException("Alphabet must contain at least 1 symbol");
        }
        Alphabet output = new Alphabet();
        for (Character c : symbols.toCharArray()) {
            output.add(c);
        }
        return output;
    }
}
