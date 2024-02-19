import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.DeterministicFiniteAutomaton;
import automata.NondeterministicFiniteAutomaton;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State q0 = new State();
        State q1 = new State();

        Set<State> states = Set.of(q0, q1);
        Alphabet alphabet = Alphabet.withSymbols("ab");

        // b*ab*
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        tf.setState(q0, 'b', q0);
        tf.setState(q0, 'a', q1);
        tf.setState(q1, 'b', q1);

        NondeterministicFiniteAutomaton nfa1 = new NondeterministicFiniteAutomaton(
                states,
                alphabet,
                tf,
                q0,
                Set.of(q1)
        );

        State r0 = new State("r0");
        State r1 = new State("r1");
        State r2 = new State("r2");
        State r3 = new State("r3");

        states = Set.of(r0, r1, r2, r3);

        // At least 3 'a's.
        tf = new Transition();
        tf.initializeFor(states, alphabet);
        tf.setState(r0, 'a', r1);
        tf.setState(r1, 'a', r2);
        tf.setState(r2, 'a', r3);
        tf.setState(r3, 'a', r3);
        tf.setState(r0, 'b', r0);
        tf.setState(r1, 'b', r1);
        tf.setState(r2, 'b', r2);
        tf.setState(r3, 'b', r3);

        NondeterministicFiniteAutomaton nfa2 = new NondeterministicFiniteAutomaton(
                states,
                alphabet,
                tf,
                r0,
                Set.of(r3)
        );

        NondeterministicFiniteAutomaton nfaCombo = AutomataCombiner.union(nfa1, nfa2);

        System.out.println(nfa1);
        System.out.println();
        System.out.println(nfa2);
        System.out.println();
        System.out.println(nfaCombo);
        System.out.println();

        for (String s : new String[]{"bab", "bababab", "bbbbbbbbbbbabbbbbb", "aab"}) {
            System.out.println(nfa1.accepts(s));
            System.out.println(nfa2.accepts(s));
            System.out.println(nfaCombo.accepts(s));
            System.out.println();
        }
    }
}
