import automata.AutomataCombiner;
import automata.DFAConvertor;
import automata.DeterministicFiniteAutomaton;
import automata.NondeterministicFiniteAutomaton;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State a = new State();
        State b = new State();
        State c = new State();

        Set<State> states = Set.of(a,b,c);
        Alphabet alphabet = Alphabet.withSymbols("abc");

        // a*b*c*
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        tf.setState(a, 'a', Set.of(a));
        tf.setState(b, 'b', Set.of(b));
        tf.setState(c, 'c', Set.of(c));

        tf.setState(a, Alphabet.EPSILON, Set.of(b));
        tf.setState(b, Alphabet.EPSILON, Set.of(c));

        NondeterministicFiniteAutomaton nfa = new NondeterministicFiniteAutomaton(
                states,
                alphabet,
                tf,
                a,
                Set.of(c)
        );

        DeterministicFiniteAutomaton dfa = DFAConvertor.convertNFA(nfa);

        System.out.println(nfa);
        System.out.println();
        System.out.println(dfa);
    }
}
