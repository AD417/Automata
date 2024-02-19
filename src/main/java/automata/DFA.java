package automata;

import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import exception.AlphabetException;
import exception.InvalidAutomatonException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Deterministic Finite Automaton. At all times, for all states, there is
 * exactly one transition, and one path from the starting point to some
 * accepting state.
 */
public record DFA(Set<State> states, Alphabet alphabet, DeterministicTransition transitionFunction, State startState,
                  Set<State> acceptingStates) {
    public DFA(
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
        if (!states.contains(startState)) {
            String msg = String.format("Invalid start state: %s is not in the state set", startState);
            throw new InvalidAutomatonException(msg);
        }
        Set<State> missingStates = acceptingStates.stream().dropWhile(states::contains).collect(Collectors.toSet());
        if (!missingStates.isEmpty()) {
            String msg = String.format(
                    "Invalid accepting state(s): %s are accepting states not in DFA set!",
                    missingStates
            );
            throw new InvalidAutomatonException(msg);
        }
        for (State state : states) {
            for (Character c : alphabet) {
                State result = transitionFunction.transition(state, c);
                if (result == null) {
                    String msg = String.format(
                            "Illegal transition: Transition δ(%s, '%c') is undefined",
                            state,
                            c
                    );
                    throw new InvalidAutomatonException(msg);
                }
                if (!states.contains(result)) {
                    String msg = String.format(
                            "Illegal transition: Transition δ(%s, '%c') is not closed (results in %s)",
                            state, c, result
                    );
                    throw new InvalidAutomatonException(msg);
                }
            }
        }
    }

    public boolean accepts(String string) {
        State state = startState;
        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                String msg = String.format(
                        "String '%s' contains symbol '%c' not in Automaton's alphabet.",
                        string, c
                );
                throw new AlphabetException(msg);
            }
            State nextState = transitionFunction.transition(state, c);
            if (nextState == null) {
                String msg = String.format(
                        "Illegal transition: Transition δ(%s, %c) is undefined!",
                        state, c
                );
                throw new InvalidAutomatonException(msg);
            }
            if (!states.contains(nextState)) {
                String msg = String.format(
                        "Illegal transition: Transition δ(%s, %c) does not map to state set!",
                        state, c
                );
                throw new InvalidAutomatonException(msg);
            }
        }
        return acceptingStates.contains(state);
    }

    @Override
    public String toString() {
        return "DFA D = (Q, Σ, δ, q0, F), where:\n" +
                "Q = " + states + "\n" +
                "Σ = " + alphabet + "\n" +
                "δ = the following table:\n" + transitionFunction +
                "q0 = " + startState + "\n" +
                "F = " + acceptingStates;
    }
}
