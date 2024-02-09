package automata;

import components.Alphabet;
import components.AutomatonCrashException;
import components.DeterministicTransition;
import components.State;

import java.util.Set;

public class DeterministicFiniteAutomaton {
    private final Set<State> states;
    private final Alphabet alphabet;
    private final DeterministicTransition transitionFunction;
    private final State startState;
    private final Set<State> acceptingStates;

    public DeterministicFiniteAutomaton(
            Set<State> states,
            Alphabet alphabet,
            DeterministicTransition transitionFunction,
            State startState,
            Set<State> acceptingStates
    ) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitionFunction = transitionFunction;
        this.startState = startState;
        this.acceptingStates = acceptingStates;
        validate();
    }

    private void validate() {
        for (State state : states) {
            for (Character c : alphabet) {
                if (transitionFunction.transition(state, c) == null) {
                    throw new AutomatonCrashException("Invalid DFA detected", "");
                }
            }
        }
    }

    public boolean accepts(String string) {
        State state = startState;
        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                throw new AutomatonCrashException("Parsed string contains character not in alphabet!", string);
            }
            state = transitionFunction.transition(state, c);
            if (state == null) {
                throw new AutomatonCrashException("State transition failure", string);
            }
        }
        return acceptingStates.contains(state);
    }
}
