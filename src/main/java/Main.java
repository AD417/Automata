import automata.DeterministicFiniteAutomaton;
import components.Alphabet;
import components.DeterministicTransition;
import components.State;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State q000 = new State("q000");
        State q001 = new State("q001");
        State q010 = new State("q010");
        State q011 = new State("q011");
        State q100 = new State("q100");
        State q101 = new State("q101");
        State q110 = new State("q110");
        State q111 = new State("q111");
        Set<State> states = Set.of(q000, q001, q010, q011, q100, q101, q110, q111);
        DeterministicTransition dt = new DeterministicTransition();
        Alphabet alphabet = Alphabet.withSymbols('a', 'b');


        dt.initializeFor(states, alphabet);

        dt.setState(q000, 'a', q000);
        dt.setState(q000, 'b', q001);
        dt.setState(q001, 'a', q010);
        dt.setState(q001, 'b', q011);
        dt.setState(q010, 'a', q100);
        dt.setState(q010, 'b', q101);
        dt.setState(q011, 'a', q110);
        dt.setState(q011, 'b', q111);
        dt.setState(q100, 'a', q000);
        dt.setState(q100, 'a', q001);
        dt.setState(q101, 'a', q010);
        dt.setState(q101, 'b', q011);
        dt.setState(q110, 'a', q100);
        dt.setState(q110, 'b', q101);
        dt.setState(q111, 'a', q110);
        dt.setState(q111, 'b', q111);

        Set<State> accepting = Set.of(q100, q101, q110, q111);

        DeterministicFiniteAutomaton dfa = new DeterministicFiniteAutomaton(states, alphabet, dt, q000, accepting);

        System.out.println(dfa.accepts(""));    // FALSE
        System.out.println(dfa.accepts("b"));   // FALSE
        System.out.println(dfa.accepts("bb"));  // FALSE
        System.out.println(dfa.accepts("bbb")); // TRUE
    }
}
