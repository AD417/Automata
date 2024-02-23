package automata;

import components.Alphabet;
import components.GeneralTransition;
import components.State;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record GNFA(Set<State> states, Alphabet alphabet, GeneralTransition transitionFunction, State startState,
                   State acceptingState) {

    @Override
    public Set<State> states() {
        return Collections.unmodifiableSet(states);
    }

    public void rip(State toRip) {
        transitionFunction.rip(toRip);
        states.remove(toRip);
    }

    public State lowestCostState() {
        State lowestState = null;
        int lowestCost = Integer.MAX_VALUE;
        for (State state : states) {
            if (acceptingState.equals(state) || startState.equals(state)) continue;
            int cost = transitionFunction.repairCost(state);
            if (cost < lowestCost) {
                lowestCost = cost;
                lowestState = state;
            }
        }

        return lowestState;
    }

    public String toRegex() {
        while (states.size() > 2) {
            State lowest = lowestCostState();
            rip(lowest);
        }
        return "" + transitionFunction.transition(startState, acceptingState);
    }

    @Override
    public String toString() {
        return "GNFA G = (Q, Σ, δ, q0, F), where:\n" +
                "Q = " + states + "\n" +
                "Σ = " + alphabet + "\n" +
                "δ = the following table:\n" + transitionFunction +
                "q0 = " + startState + "\n" +
                "F = " + acceptingState;
    }
}
