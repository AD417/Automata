import automata.DeterministicFiniteAutomaton;
import automata.NondeterministicFiniteAutomaton;
import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import components.Transition;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State loop = new State();
        State b0 = new State();
        State b1 = new State();
        State b2 = new State();
        Set<State> states = Set.of(loop, b0, b1, b2);
        Alphabet alphabet = Alphabet.withSymbols('a', 'b');
        Transition dt = new Transition();

        dt.initializeFor(states, alphabet);
        dt.setState(loop, 'a', loop);
        dt.setState(loop, 'b', loop, b0);
        dt.setState(b0, 'a', b1);
        dt.setState(b0, 'b', b1);
        dt.setState(b1, 'a', b2);
        dt.setState(b1, 'b', b2);

        NondeterministicFiniteAutomaton nfa =
                new NondeterministicFiniteAutomaton(states, alphabet, dt, loop, Set.of(b2));

        System.out.println(nfa.accepts(""));
        System.out.println(nfa.accepts("abb"));
        System.out.println(nfa.accepts("bbb"));
        System.out.println(nfa.accepts("ababababababbabababab"));
        System.out.println(nfa.accepts("bbbbbbbbbbbbbbbbbbbbbbbbbbaaa"));
    }
}
