package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.components.State;
import automata.components.Transition;
import automata.exception.AlphabetException;

import java.util.Set;
import java.util.stream.Collectors;

public record ChoiceToken(Set<Character> symbols) implements Token {
    public ChoiceToken(String symbols) {
        this(symbols.chars().mapToObj(x -> (char)x).collect(Collectors.toSet()));
    }


    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        State initial = new State();
        State accepting = new State();

        Set<State> states = Set.of(initial, accepting);
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        for (Character symbol : symbols) {
            if (!alphabet.contains(symbol)) {
                String msg = String.format(
                        "Symbol list %s contains symbol '%c' not in alphabet.",
                        symbols, symbol
                );
                throw new AlphabetException(msg);
            }
            tf.setState(initial, symbol, accepting);
        }
        return new NFA(states, alphabet, tf, initial, Set.of(accepting));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Character s : symbols) sb.append(s);
        sb.append(']');
        return sb.toString();
    }
}
