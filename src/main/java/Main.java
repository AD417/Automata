import automata.PDA;
import automata.components.Alphabet;
import automata.components.StackState;
import automata.components.StackTransition;
import automata.components.State;

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
        st.setState(start, Alphabet.EPSILON, Alphabet.EPSILON, mid, '$');
        // If we read an 'a', we can add an "A"...
        st.setState(mid, Alphabet.EPSILON, 'a', mid, 'A');
        // ...or remove a "B", if possible.
        st.setState(mid, 'B', 'a', mid, Alphabet.EPSILON);
        // If we read a 'b', we can add a "B"...
        st.setState(mid, Alphabet.EPSILON, 'b', mid, 'B');
        // Or remove an "A", if possible.
        st.setState(mid, 'A', 'b', mid, Alphabet.EPSILON);
        // If the only thing on the stack is the initial control char,
        // Then we can move to the accept state.
        st.setState(mid, '$', Alphabet.EPSILON, end, Alphabet.EPSILON);

        Set<State> accepting = Set.of(end);

        // "Accept all strings where #'a' == #'b'.
        PDA pushover = new PDA(states, alphabet, stackAlphabet, st, start, accepting);

        st.entrySet().forEach(System.out::println);
        System.out.println();
        System.out.println(pushover.accepts(""));
        System.out.println(pushover.accepts("ab"));
        System.out.println(pushover.accepts("ba"));
        System.out.println(pushover.accepts("aaaabbbb"));
        System.out.println(pushover.accepts("bababababbaab"));
        System.out.println(pushover.accepts("a"));
    }
}
