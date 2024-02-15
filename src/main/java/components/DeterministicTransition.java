package components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class DeterministicTransition extends HashMap<State, HashMap<Character, State>> {
    public void initializeFor(Set<State> states, Alphabet alphabet) {
        for (State state : states) {
            HashMap<Character, State> stateFunction = computeIfAbsent(state, k -> new HashMap<>());
            alphabet.forEach(chr -> stateFunction.put(chr, state));
        }
    }
    public void setState(State currentState, Character chr, State nextState) {
        getOrDefault(currentState, new HashMap<>()).put(chr, nextState);
    }

    public State transition(State currentState, Character chr) {
        return getOrDefault(currentState, new HashMap<>()).get(chr);
    }

    public Set<State> getStates() {
        return Collections.unmodifiableSet(keySet());
    }
}
