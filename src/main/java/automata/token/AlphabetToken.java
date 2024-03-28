package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.components.State;
import automata.components.Transition;

import java.util.Set;

public record AlphabetToken() implements Token{
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        State initial = new State();
        State accepting = new State();
        Set<State> states = Set.of(initial, accepting);
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        for (Character symbol : alphabet) {
            tf.setState(initial, symbol, accepting);
        }
        return new NFA(states, alphabet, tf, initial, Set.of(accepting));
    }
}
