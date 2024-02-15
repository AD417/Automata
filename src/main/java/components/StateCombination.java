package components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StateCombination extends HashMap<State, HashMap<State, State>> {
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

    public State getCombo(State state1, State state2) {
        return getOrDefault(state1, new HashMap<>()).get(state2);
    }

    public Set<State> getAllStates() {
        Set<State> out = new HashSet<>();
        this.values().stream()
                .map(HashMap::values)
                .forEach(out::addAll);

        return out;
    }
}
