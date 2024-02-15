import automata.AutomataCombiner;
import automata.DeterministicFiniteAutomaton;
import automata.NondeterministicFiniteAutomaton;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State trap = new State("TRAP");
        State a0 = new State("a0");
        State a1 = new State("a1");
        Alphabet alphabet = Alphabet.withSymbols("ab");

        Set<State> states = Set.of(trap, a0, a1);
        DeterministicTransition dt = new DeterministicTransition();
        dt.initializeFor(states, alphabet);
        // Accepts strings beginning with a.
        dt.setState(a0, 'b', trap);
        dt.setState(a0, 'a', a1);

        Set<State> acceptingStates = Set.of(a1);

        DeterministicFiniteAutomaton dfa1 = new DeterministicFiniteAutomaton(states, alphabet, dt, a0, acceptingStates);

        State b0 = new State("b0");
        State b1 = new State("b1");
        states = Set.of(b0, b1);

        dt = new DeterministicTransition();
        dt.initializeFor(states, alphabet);
        // Accepts string ending with b.
        dt.setState(b0, 'b', b1);
        dt.setState(b1, 'a', b0);

        acceptingStates = Set.of(b1);

        DeterministicFiniteAutomaton dfa2 = new DeterministicFiniteAutomaton(states, alphabet, dt, b0, acceptingStates);

        DeterministicFiniteAutomaton dfaCombo = AutomataCombiner.intersection(dfa1, dfa2);

        System.out.println(dfaCombo.accepts("ab"));                     // TRUE
        System.out.println(dfaCombo.accepts("a"));                      // FALSE
        System.out.println(dfaCombo.accepts("b"));                      // FALSE
        System.out.println(dfaCombo.accepts("aaabbbbb"));               // TRUE
        System.out.println(dfaCombo.accepts("ababababbabababababa"));   // FALSE
        System.out.println(dfaCombo.accepts("bbabababbabababababb"));   // FALSE
        System.out.println(dfaCombo.accepts("ababababbabababababb"));   // TRUE
    }
}
