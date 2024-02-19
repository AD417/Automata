import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.NFA;
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
        tf.setState(q0, 'a', q1);

        NFA nfa = new NFA(states, alphabet, tf, q0, Set.of(q1));

        System.out.println(nfa);
        System.out.println();
        System.out.println(AutomataConvertor.NFAtoDFA(nfa));
        System.out.println();
        System.out.println(AutomataCombiner.power(nfa, 2));
        System.out.println();
        NFA nfa1 = AutomataCombiner.power(nfa, 3);
        System.out.println(nfa1);
        System.out.println();
        System.out.println(nfa1.simplifyEpsilon());
    }
}
