package automata;

import components.Alphabet;
import components.State;
import components.Transition;
import exception.AlphabetException;
import exception.InvalidAutomatonException;

import java.util.*;
import java.util.stream.Collectors;

public record NFA(Set<State> states, Alphabet alphabet, Transition transitionFunction, State startState,
                  Set<State> acceptingStates) {
    public NFA(
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
        validate();
    }

    @Override
    public Set<State> states() {
        return Collections.unmodifiableSet(states);
    }

    @Override
    public Set<State> acceptingStates() {
        return Collections.unmodifiableSet(acceptingStates);
    }

    private void validate() {
        if (!states.contains(startState)) {
            String msg = String.format("Invalid start state: %s is not in the state set", startState);
            throw new InvalidAutomatonException(msg);
        }
        Set<State> missingStates = acceptingStates.stream().dropWhile(states::contains).collect(Collectors.toSet());
        if (!missingStates.isEmpty()) {
            String msg = String.format(
                    "Invalid accepting state(s): %s are accepting states not in NFA set!",
                    missingStates
            );
            throw new InvalidAutomatonException(msg);
        }
        for (State state : states) {
            for (Character c : alphabet) {
                Set<State> output = transitionFunction.transition(state, c);
                // Null is acceptable, but not really preferred.
                // TODO: Warning?
                if (output == null) continue;
                Set<State> badStates = output.stream().filter(x -> !states.contains(x)).collect(Collectors.toSet());
                if (badStates.isEmpty()) continue;

                String msg = String.format(
                        "Illegal transition: Transition δ(%s, '%c') is not closed (results in unclosed states %s)",
                        state, c, badStates
                );
                throw new InvalidAutomatonException(msg);
            }
        }
    }

    public boolean accepts(String string) {
        Set<State> currentStates = new HashSet<>();
        currentStates.add(startState);
        currentStates = epsilonClosure(currentStates);

        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                String msg = String.format("String '%s' contains symbol '%c' not in Automaton's alphabet.", string, c);
                throw new AlphabetException(msg);
            }
            Set<State> immediateNextStates = new HashSet<>();
            currentStates.stream()
                    .map(state -> transitionFunction.transition(state, c))
                    .filter(Objects::nonNull)
                    .forEach(immediateNextStates::addAll);

            currentStates = epsilonClosure(immediateNextStates);
        }
        return acceptingStates.stream().anyMatch(currentStates::contains);
    }

    public Set<State> epsilonClosure(State state) {
        return epsilonClosure(Set.of(state));
    }

    public Set<State> epsilonClosure(Set<State> states) {
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

    @Override
    public String toString() {
        return "NFA N = (Q, Σ, δ, q0, F), where:\n" +
                "Q = " + states + "\n" +
                "Σ = " + alphabet + "\n" +
                "δ = the following table:\n" + transitionFunction +
                "q0 = " + startState + "\n" +
                "F = " + acceptingStates;
    }
}
