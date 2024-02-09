package components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Transition extends HashMap<State, HashMap<Character, Set<State>>> {

    public void initializeFor(Set<State> states, Alphabet alphabet) {
        for (State state : states) {
            computeIfAbsent(state, k -> new HashMap<>());
        }
    }

    public void setState(State currentState, Character chr, State nextState, State ...otherStates) {
        Set<State> states = new HashSet<>(Arrays.asList(otherStates));
        states.add(nextState);
        getOrDefault(currentState, new HashMap<>()).put(chr, states);
    }

    public void setState(State currentState, Character chr, Set<State> nextStates) {
        getOrDefault(currentState, new HashMap<>()).put(chr, nextStates);
    }

    public Set<State> transition(State currentState, Character chr) {
        return getOrDefault(currentState, new HashMap<>()).get(chr);
    }
}
