package automata.components;

import automata.exception.AlphabetException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StackAlphabet extends HashSet<String> {
    /**
     * An unused control character representing no change to a stack in a PDA.
     * Epsilon stack changes will increase or decrease the stack's size.
     */
    public static final String EPSILON = "ε"; // '\uFFFF';

    /**
     * An otherwise unused control character representing the bottom of
     * the stack. Can be used by PDAs to determine if a stack is about to
     * become empty.
     */
    public static final String CONTROL = "$";

    public static StackAlphabet withSymbols(Set<String> alphabet) {
        StackAlphabet output = new StackAlphabet();
        output.addAll(alphabet);
        return output;
    }
    /**
     * Create a stack alphabet containing the provided list of characters.
     * @param firstSymbol one of the symbols in the alphabet. An alphabet must
     *                    consist of at least one symbol.
     * @param otherSymbols the remaining symbols in the alphabet. An alphabet
     *                     may contain any number of unique symbols.
     * @return an Alphabet containing the given symbols.
     */
    public static StackAlphabet withSymbols(String firstSymbol, String ... otherSymbols) {
        StackAlphabet output = new StackAlphabet();
        output.add(firstSymbol);
        output.addAll(Arrays.asList(otherSymbols));

        return output;
    }

    /**
     * Create a Stack Alphabet containing the provided string of characters.
     * @param symbols a single string containing all the symbols, and only the
     *                symbols in the alphabet. Must contain at least 1 symbol.
     * @return a Stack Alphabet containing the given symbols.
     */
    public static StackAlphabet withSymbols(String symbols) {
        if (symbols.isEmpty()) {
            throw new AlphabetException("Alphabet must contain at least 1 symbol");
        }
        StackAlphabet output = new StackAlphabet();
        for (Character c : symbols.toCharArray()) {
            output.add("" + c);
        }
        return output;
    }

}
