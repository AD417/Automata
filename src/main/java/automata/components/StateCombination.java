package automata.components;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A state mapper for showing all possible combination states for
 * the state sets in two disparate DFAs.
 */
public class StateCombination extends HashMap<State, HashMap<State, State>> {
    /**
     * Initialize a State Combination for two sets of states.
     * @param firstState The first set of states.
     * @param secondState The second set of states.
     * @return A mapping function mapping any combination of states to a third,
     * combined state.
     */
    public static StateCombination createFor(Set<State> firstState, Set<State> secondState) {
        StateCombination sc = new StateCombination();
        firstState.forEach(state1 -> {
            HashMap<State, State> stateMap = sc.computeIfAbsent(state1, k -> new HashMap<>());
            secondState.forEach(state2 -> {
                State stateCombo = new State(state1.getName() + "_" + state2.getName());
                stateMap.put(state2, stateCombo);
            });
            sc.put(state1, stateMap);
        });
        return sc;
    }

    /**
     * Determine a combination of states.
     * @param state1 A state in the first state set in this combo.
     * @param state2 A state in the second state set in this combo.
     * @return The state designated as the combination of the two states.
     */
    public State getCombo(State state1, State state2) {
        return getOrDefault(state1, new HashMap<>()).get(state2);
    }

    /**
     * Get the set containing all possible output states in this mapping
     * function.
     * @return A set containing all possible combination states.
     */
    public Set<State> getAllStates() {
        return this.values().stream()
                .flatMap(x -> x.values().stream())
                .collect(Collectors.toSet());
    }
}
