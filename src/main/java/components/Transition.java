package components;

import java.util.*;

/**
 * The transition function a Nondeterministic Finite Automaton. This "function"
 * maps from the cartesian product of all States in a NFA and all characters in
 * an alphabet to the set of all subsets of all states in the same NFA.
 *
 * Under the hood, this isn't actually a function, but is actually a series
 * of {@link HashMap}s that give the same result.
 */
public class Transition extends HashMap<State, HashMap<Character, Set<State>>> {
    /**
     * Initialize this transition function. This will DESTROY any data already
     * present in this transition function, and reset it so that there is a
     * valid output for every input state and alphabet symbol.
     * <p>
     * Note that, for all inputs, `d(q ∈ Q, s ∈ A) -> ∅`; that is, initially,
     * all states will map to nothing for any alphabet symbol.
     * @param states The set of all States that this transition function should
     *               be able to handle.
     * @param alphabet The alphabet containing all symbols this transition
     *                 function should be able to handle.
     */
    public void initializeFor(Set<State> states, Alphabet alphabet) {
        Set<State> DEFAULT = new HashSet<>();
        for (State state : states) {
            HashMap<Character, Set<State>> transitionMap = computeIfAbsent(state, k -> new HashMap<>());
            for (Character c : alphabet) {
                transitionMap.put(c, DEFAULT);
            }
            transitionMap.put(Alphabet.EPSILON, DEFAULT);
        }
    }

    /**
     * Set the output of this transition function for an input state and
     * symbol. This overwrites any previous transitions.
     * @param currentState The input state for this transition rule.
     * @param chr The input symbol for this transition rule.
     * @param nextStates The valid output states for this transition rule.
     */
    public void setState(State currentState, Character chr, State ...nextStates) {
        Set<State> states = new HashSet<>(Arrays.asList(nextStates));
        getOrDefault(currentState, new HashMap<>()).put(chr, states);
    }

    /**
     * Set the output of this transition function for an input state and symbol.
     * @param currentState The input state for this transition rule.
     * @param chr The input symbol for this transition rule.
     * @param nextStates The valid output states for this transition rule.
     */
    public void setState(State currentState, Character chr, Set<State> nextStates) {
        getOrDefault(currentState, new HashMap<>()).put(chr, nextStates);
    }

    /**
     * Get the output of this transition function for an input state
     * and symbol.
     * @param currentState The input state for this transition function.
     * @param chr The input symbol for this transition function.
     * @return The output states for this transition function,
     * given the inputs.
     */
    public Set<State> transition(State currentState, Character chr) {
        return getOrDefault(currentState, new HashMap<>()).get(chr);
    }

    /**
     * Get all the states this transition function can handle.
     * @return all off the states this transition function can handle.
     */
    public Set<State> getStates() {
        return Collections.unmodifiableSet(keySet());
    }

    public void addAllTo(Transition other) {
        for (State state : keySet()) {
            HashMap<Character, Set<State>> stateProduct = get(state);
            for (Character symbol : stateProduct.keySet()) {
                other.setState(state, symbol, transition(state, symbol));
            }
        }
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
            if (c == Alphabet.EPSILON) c = 'ε';
            sb.append(" |").append(String.format(formatter, c));
            lineLength += longest + 2;
        }
        sb.append('\n');
        sb.append("-".repeat(Math.max(0, lineLength)));
        sb.append('\n');
        states.stream().sorted().forEach(state -> {
            sb.append(String.format(formatter, state));
            // TODO: check what happens if the transition output is longer
            //  than any input.
            for (Character c : alphabet) {
                String transitionStr = transition(state, c).toString();
                if (transitionStr.equals("[]")) transitionStr = "";
                sb.append(" |").append(String.format(formatter, transitionStr));
            }
            sb.append('\n');
        });

        return sb.toString();
    }
}
