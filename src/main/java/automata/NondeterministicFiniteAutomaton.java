package automata;

import components.Alphabet;
import components.AutomatonCrashException;
import components.State;
import components.Transition;

import java.util.*;

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
        currentStates = epsilonClosure(currentStates);

        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                throw new AutomatonCrashException("Parsed string contains a character not in alphabet!", string);
            }
            Set<State> immediateNextStates = new HashSet<>();
            currentStates.stream()
                    .map(state -> transitionFunction.transition(state, c))
                    .filter(Objects::nonNull)
                    .forEach(immediateNextStates::addAll);

            // TODO: epsilon transitions.
            currentStates = epsilonClosure(immediateNextStates);
        }
        return acceptingStates.stream().anyMatch(currentStates::contains);
    }

    private Set<State> epsilonClosure(Set<State> states) {
        Queue<State> toClose = new LinkedList<>(states);
        Set<State> output = new HashSet<>(states);
        while (!toClose.isEmpty()) {
            State currentState = toClose.poll();
            for (State epsilonState : transitionFunction.transition(currentState, Alphabet.EPSILON)) {
                if (output.contains(epsilonState)) continue;
                output.add(epsilonState);
                toClose.add(epsilonState);
            }
        }
        return output;
    }
}
