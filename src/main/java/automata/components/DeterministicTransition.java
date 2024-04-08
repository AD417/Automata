package automata.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * The transition function a Deterministic Finite Automaton. This "function"
 * maps from the cartesian product of all States in a DFA and all characters in
 * an alphabet to the set of all states in the same DFA. <br>
 * Under the hood, this isn't actually a function, but is actually a series
 * of {@link HashMap}s that give the same result.
 */
public class DeterministicTransition extends HashMap<State, HashMap<Character, State>> {
    /**
     * Initialize this transition function. This will DESTROY any data already
     * present in this transition function, and reset it so that there is a
     * valid output for every input state and alphabet symbol.
     * <p>
     * Note that, for all inputs, `d(q ∈ Q, s ∈ A) -> q`; that is, initially,
     * all states will map to themselves for any alphabet symbol.
     * @param states The set of all States that this transition function should
     *               be able to handle.
     * @param alphabet The alphabet containing all symbols this transition
     *                 function should be able to handle.
     */
    public void setDefaults(Set<State> states, Alphabet alphabet) {
        for (State state : states) {
            HashMap<Character, State> stateFunction = computeIfAbsent(state, k -> new HashMap<>());
            alphabet.forEach(chr -> stateFunction.put(chr, state));
        }
    }

    /**
     * Copy the transitions present in the other transition function into this
     * one. Assumes that the states and alphabet in this transition function
     * are a subset of those found in the other function.
     * @param other Another deterministic transition fucntion. Assumed to
     *              have the same alphabet as, and at least all the states
     *              in this transition function.
     * @param alphabet the Alphabet used in this transition function. Assumed
     *                 to be the same in both functions.
     */
    public void copyFrom(DeterministicTransition other, Alphabet alphabet) {
        for (State state : other.getStates()) {
            for (Character symbol : alphabet) {
                this.setState(state, symbol, other.transition(state, symbol));
            }
        }
    }

    /**
     * Set the output of this transition function for an input state and symbol.
     * @param currentState The input state for this transition rule.
     * @param chr The input symbol for this transition rule.
     * @param nextState The output state for this transition rule.
     */
    public void setState(State currentState, Character chr, State nextState) {
        computeIfAbsent(currentState, k -> new HashMap<>()).put(chr, nextState);
    }

    /**
     * Get the output of this transition function for an input state
     * and symbol.
     * @param currentState The input state for this transition function.
     * @param chr The input symbol for this transition function.
     * @return The output state for this transition function, given the inputs.
     */
    public State transition(State currentState, Character chr) {
        return getOrDefault(currentState, new HashMap<>()).get(chr);
    }

    /**
     * Get all the states this transition function can handle.
     * @return all off the states this transition function can handle.
     */
    public Set<State> getStates() {
        return Collections.unmodifiableSet(keySet());
    }

    @Override
    public String toString() {
        if (size() == 0) return "{EMPTY}";
        Set<State> states = getStates();
        Set<Character> alphabet = get(states.stream().findFirst().orElseThrow()).keySet();

        int longest = states.stream().mapToInt(x -> x.getName().length()).max().orElseThrow();
        if (longest < 5) longest = 5;
        longest++;
        String formatter = "%"+longest+"s";

        int lineLength = longest;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatter, "STATE"));
        for (Character c : alphabet) {
            sb.append(" |").append(String.format(formatter, c));
            lineLength += longest + 2;
        }
        sb.append('\n');
        sb.append("-".repeat(Math.max(0, lineLength)));
        sb.append('\n');
        states.stream().sorted().forEach(state -> {
            sb.append(String.format(formatter, state));
            for (Character c : alphabet) {
                sb.append(" |").append(String.format(formatter, transition(state, c)));
            }
            sb.append('\n');
        });

        return sb.toString();
    }
}
