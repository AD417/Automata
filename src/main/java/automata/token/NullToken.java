package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.components.State;
import automata.components.Transition;

import java.util.HashSet;
import java.util.Set;

public record NullToken() implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        State state = new State();
        Set<State> states = Set.of(state);

        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);

        return new NFA(states, alphabet, tf, state, new HashSet<>());
    }

    @Override
    public String toString() {
        return "<>";
    }
}
