package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.components.State;
import automata.components.Transition;

import java.util.Set;

public record EmptyToken() implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        State state = new State();
        Set<State> states = Set.of(state);

        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);

        return new NFA(states, alphabet, tf, state, states);
    }

    @Override
    public String toString() {
        return "";
    }
}
