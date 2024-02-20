package automata.token;

import automata.NFA;
import components.Alphabet;
import components.State;
import components.Transition;
import exception.AlphabetException;

import java.util.Set;

public record CharacterToken(char symbol) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        if (!alphabet.contains(symbol)) {
            String msg = String.format(
                    "Symbol '%c' is not in alphabet!",
                    symbol
            );
            throw new AlphabetException(msg);
        }

        State initial = new State();
        State accepting = new State();

        Set<State> states = Set.of(initial, accepting);
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        tf.setState(initial, symbol, accepting);

        return new NFA(states, alphabet, tf, initial, Set.of(accepting));
    }

    @Override
    public String toString() {
        return "'"+symbol+"'";
    }
}
