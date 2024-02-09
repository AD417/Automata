package automata;

import components.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NondeterministicFiniteAutomaton {
    private final Set<State> states;
    private final Alphabet alphabet;
    private final Transition transitionFunction;
    private final State startState;
    private final Set<State> acceptingStates;

    public NondeterministicFiniteAutomaton(
            Set<State> states,
            Alphabet alphabet,
            Transition transitionFunction,
            State startState,
            Set<State> acceptingStates
    ) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitionFunction = transitionFunction;
        this.startState = startState;
        this.acceptingStates = acceptingStates;
    }

    private void validate() {
        for (State state : states) {
            for (Character c : alphabet) {
                Set<State> output = transitionFunction.transition(state, c);
                if (output == null) continue;
                boolean valid = states.containsAll(output);
                if (!valid) throw new AutomatonCrashException("Invalid NFA detected", "");
            }
        }
    }

    public boolean accepts(String string) {
        Set<State> currentStates = new HashSet<>();
        currentStates.add(startState);

        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                throw new AutomatonCrashException("Parsed string contains a character not in alphabet!", string);
            }
            Set<State> futureStates = new HashSet<>();
            currentStates.stream()
                    .map(state -> transitionFunction.transition(state, c))
                    .filter(Objects::nonNull)
                    .forEach(futureStates::addAll);

            // TODO: epsilon transitions.
            currentStates = futureStates;
        }
        return acceptingStates.stream().anyMatch(currentStates::contains);
    }
}
