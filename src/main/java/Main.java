import automata.PDA;
import automata.components.Alphabet;
import automata.components.StackState;
import automata.components.StackTransition;
import automata.components.State;
import grammar.CFG;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");
        Set<Character> stackAlphabet = Set.of('$', 'A', 'B');

        State start = new State("START");
        State mid = new State("Q");
        State end = new State("FINAL");

        Set<State> states = Set.of(start, mid, end);

        StackTransition st = new StackTransition();
        st.initializeFor(states, stackAlphabet, alphabet);

        // Add a starting control character.
        StackState now = new StackState(start, Alphabet.EPSILON);
        StackState next = new StackState(mid, '$');
        st.setState(now, Alphabet.EPSILON, next);

        // If we read an 'a', we can add an "A"...
        now = new StackState(mid, Alphabet.EPSILON);
        next = new StackState(mid, 'A');
        st.setState(now, 'a', next);

        // ...or remove a "B", if possible.
        next = new StackState(mid, 'B');
        st.setState(now, 'b', next);

        // If we read a 'b', we can add a "B"...
        now = new StackState(mid, 'A');
        next = new StackState(mid, Alphabet.EPSILON);
        st.setState(now, 'b', next);

        // Or remove an "A", if possible.
        now = new StackState(mid, 'B');
        next = new StackState(mid, Alphabet.EPSILON);
        st.setState(now, 'a', next);

        // If the only thing on the stack is the initial control char,
        // Then we can move to the accept state.
        now = new StackState(mid, '$');
        next = new StackState(end, Alphabet.EPSILON);
        st.setState(now, Alphabet.EPSILON, next);

        Set<State> accepting = Set.of(end);

        // "Accept all strings where #'a' == #'b'.
        PDA pushover = new PDA(states, alphabet, stackAlphabet, st, start, accepting);

        st.entrySet().forEach(System.out::println);
        System.out.println();
        System.out.println(pushover.accepts("bbbbaaaa"));
    }
}
