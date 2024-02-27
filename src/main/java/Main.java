import automata.DFA;
import automata.GNFA;
import automata.NFA;
import components.Alphabet;
import components.DeterministicTransition;
import components.State;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        State q0 = new State("q0");
        State q1 = new State("q1");

        Set<State> states = Set.of(q0, q1);

        Alphabet alphabet = Alphabet.withSymbols("ab");

        DeterministicTransition dt = new DeterministicTransition();
        dt.setDefaults(states, alphabet);

        dt.setState(q0, 'b', q1);
        dt.setState(q1, 'a', q0);

        Set<State> accepting = Set.of(q0);

        DFA d = new DFA(states, alphabet, dt, q0, accepting);

        System.out.println(d);

        System.out.println(d.toGNFA());

        System.out.println(d.toNFA().toGNFA());

        /*g = d.toGNFA();
        g.rip(q1);
        System.out.println(g.toRegex());*/
    }
}
