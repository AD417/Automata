package automata.components;

import java.util.*;

/**
 * The transition function of a Pushdown Automaton. This
 * "function" maps from the cartesian product of all States in a NFA and all
 * characters in an alphabet to the set of all subsets of all states in the
 * same NFA.
 * Under the hood, this isn't actually a function, but is actually a series
 * of {@link HashMap}s that give the same result.
 */
public class StackTransition extends HashMap<StackState, HashMap<Character, Set<StackState>>> {
    /**
     * Initialize this transition function. This will DESTROY any data already
     * present in this transition function, and reset it so that there is a
     * valid output for every input state and alphabet symbol.
     * <p>
     * Note that, for all inputs, `d(q ∈ Q, l ∈ ?, s ∈ A) -> ∅`; that is, initially,
     * all states will map to nothing for any alphabet symbol.
     * @param states The set of all States that this transition function should
     *               be able to handle.
     * @param alphabet The alphabet containing all symbols this transition
     *                 function should be able to handle.
     */
    public void initializeFor(Set<State> states, Set<String> stackSymbols, Alphabet alphabet) {
        clear();
        Set<StackState> DEFAULT = Collections.unmodifiableSet(new HashSet<>());
        stackSymbols = new HashSet<>(stackSymbols);
        stackSymbols.add("" + Alphabet.EPSILON); // VERY BAND-AID FIX
        for (State state : states) {
            for (String stackSymbol : stackSymbols) {
                StackState input = new StackState(state, stackSymbol);
                for (Character symbol : alphabet) setState(input, symbol, DEFAULT);
                setState(input, Alphabet.EPSILON, DEFAULT);
            }
        }
    }

    /**
     * Set the output of this transition function using the full
     * (Q x Σ x Γ) --> (Q x Γ) definition.
     * @param stateIn The input state.
     * @param stackIn The input stack symbol (popped from the stack).
     *                May be epsilon (Nothing).
     * @param tapeChar The input tape character.
     *                 May be epsilon (epsilon transition)
     * @param stateOut The output state.
     * @param stackOut The output stack symbol (pushed to the stack).
     *                 May be epsilon (Nothing).
     */
    public void setState(State stateIn, String stackIn, Character tapeChar,
                         State stateOut, String stackOut) {
        setState(new StackState(stateIn, stackIn),
                tapeChar,
                new StackState(stateOut, stackOut));
    }

    /**
     * Set the output of this transition function for an input state and
     * symbol. This overwrites all previous transitions for this input.
     * @param currentState The input state for this transition rule.
     * @param chr The input symbol for this transition rule.
     * @param results The valid output states and stack characters for this transition rule.
     */
    public void setState(StackState currentState, Character chr, StackState ...results) {
        Set<StackState> states = new HashSet<>(Arrays.asList(results));
        computeIfAbsent(currentState, k -> new HashMap<>()).put(chr, states);
    }

    /**
     * Set the output of this transition function for an input state and symbol.
     * @param currentState The input state for this transition rule.
     * @param chr The input symbol for this transition rule.
     * @param nextStates The valid output states for this transition rule.
     */
    public void setState(StackState currentState, Character chr, Set<StackState> nextStates) {
        computeIfAbsent(currentState, k -> new HashMap<>()).put(chr, nextStates);
    }

    /**
     * Get the output of this transition function for an input state
     * and symbol.
     * @param currentState The input state for this transition function.
     * @param chr The input symbol for this transition function.
     * @return The output states for this transition function,
     * given the inputs.
     */
    public Set<StackState> transition(StackState currentState, Character chr) {
        return getOrDefault(currentState, new HashMap<>()).getOrDefault(chr, new HashSet<>());
    }

    /**
     * Get the output of this transition function that ignores the stack.
     * @param currentState The current State the automata is at.
     * @param chr the input symbol for this transition function.
     * @return Output states that do not pop the stack.
     */
    public Set<StackState> epsilonStackTransition(State currentState,
                                                  Character chr) {
        StackState equivalent = new StackState(currentState, ""+Alphabet.EPSILON);
        return getOrDefault(equivalent, new HashMap<>())
                .getOrDefault(chr, new HashSet<>());
    }

    /**
     * Get all the states this transition function can handle.
     * @return all off the states this transition function can handle.
     */
    public Set<StackState> getStates() {
        return Collections.unmodifiableSet(keySet());
    }

    public void addAllTo(StackTransition other) {
        for (StackState state : keySet()) {
            HashMap<Character, Set<StackState>> stateProduct = get(state);
            for (Character symbol : stateProduct.keySet()) {
                other.setState(state, symbol, transition(state, symbol));
            }
        }
    }

    @Override
    public String toString() {
        //return super.toString();
        if (size() == 0) return "{EMPTY}\n";
        Set<StackState> states = getStates();
        Set<Character> alphabet = get(states.stream().findFirst().orElseThrow()).keySet();

        int longest = states.stream().mapToInt(x -> x.toString().length()).max().orElseThrow();
        if (longest < 10) longest = 10;
        longest += 4;
        String formatter = "%-"+longest+"s";

        int lineLength = longest;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatter, "q(in), [Γ], s "));
        sb.append("--> q(out), [Γ]\n");
        sb.append("-".repeat(longest + 15)).append("\n");

        states.stream().sorted().forEachOrdered(state -> {
            String asStr = state.toString();
            HashMap<Character, Set<StackState>> map = get(state);
            map.keySet().stream().sorted().forEachOrdered(x -> {
                for (StackState s : map.get(x)) {
                    String unformatted = asStr + ", " + x;
                    sb.append(String.format(formatter, unformatted)).append("--> ");
                    sb.append(s);
                    sb.append('\n');
                }
            });
        });

        return sb.toString();
    }
}
