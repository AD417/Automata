package automata.components;

import automata.token.*;
import automata.exception.InvalidStateException;

import java.util.*;

/**
 * The transition function of a Generalized NFA. This "Function" dictates all
 * the valid Regex strings that map from any state in the GNFA to any other
 * state.
 * Since there is only one transition between states, and the transition
 * "Token"s can be anything, it's more consistent to make the function take in
 * the current and future state and determine the required transition.
 */
public class GeneralTransition extends HashMap<State, HashMap<State, Token>> {
    /**
     * Initialize this transition function. This will DESTROY any data already
     * present in this transition function, and reset it so that there is a
     * valid output for every input state and alphabet symbol.
     * @param states The set of all States that this transition function should
     *               be able to handle.
     */
    public void initializeFor(Set<State> states) {
        clear();
        Token DEFAULT = new EmptyToken();
        for (State state : states) {
            HashMap<State, Token> transitionMap = new HashMap<>();
            put(state, transitionMap);
            for (State nextState : states) {
                transitionMap.put(nextState, DEFAULT);
            }
        }
    }

    /**
     * Set up this transition function to be equivalent to the provided
     * transition function.
     * @param tf a transition function to make this function equivalent to.
     */
    public void convertFrom(Transition tf, Alphabet alphabet) {
        Set<Character> actualAlphabet = new HashSet<>();
        actualAlphabet.add(Alphabet.EPSILON);
        actualAlphabet.addAll(alphabet);
        initializeFor(tf.getStates());
        for (State currentState : tf.getStates()) {
            HashMap<State, Set<Character>> choices = new HashMap<>();
            // Quasi-Efficient collection strategy.
            for (Character symbol : actualAlphabet) {
                tf.transition(currentState, symbol).forEach(x -> {
                    choices.computeIfAbsent(x, k -> new HashSet<>()).add(symbol);
                });
            }
            HashMap<State, Token> transition = get(currentState);
            for (State futureState : tf.getStates()) {
                // Determine ways to convert from the current state to the future state.
                Set<Character> choice = choices.get(futureState);
                // Special case: if there is no way to do so, stub it with a null transition.
                if (Objects.isNull(choice)) {
                    transition.put(futureState, new NullToken());
                } else if (choice.size() == 1) {
                    char symbol = choice.stream().findFirst().orElseThrow();
                    if (symbol == Alphabet.EPSILON) {
                        transition.put(futureState, new EmptyToken());
                    } else {
                        transition.put(futureState, new CharacterToken(symbol));
                    }
                } else {
                    transition.put(futureState, new ChoiceToken(choice));

                }
            }
        }
    }

    /**
     * Set the transition between these states.
     * @param currentState The input state for the transition.
     * @param futureState The output state for the transition
     * @param transition The Tokenized Regex for this transition.
     */
    public void setTransition(State currentState, State futureState, Token transition) {
        computeIfAbsent(currentState, k -> new HashMap<>()).put(futureState, transition);
    }

    /**
     * Get the regex required for a valid transition from the current state
     * to the next one.
     * @param currentState The input state for this transition function.
     * @param futureState The input symbol for this transition function.
     * @return The output states for this transition function,
     * given the inputs.
     */
    public Token transition(State currentState, State futureState) {
        return getOrDefault(currentState, new HashMap<>()).get(futureState);
    }

    /**
     * Determine the amount of repairing required to repair the GNFA transition
     * if a given state is ripped out.
     * @param toRemove The state that would be removed.
     * @return The number of transitions that would be updated if this state
     * was ripped out. A lower number is better.
     */
    public int repairCost(State toRemove) {
        if (!getStates().contains(toRemove)) {
            throw new InvalidStateException("State '" + toRemove + "' is not in GNFA!");
        }
        int goingOut = 0;
        int goingIn = 0;
        Token NULL = new NullToken();
        for (State state : getStates()) {
            if (state.equals(toRemove)) continue;
            if (!transition(toRemove, state).equals(NULL)) goingOut++;
            if (!transition(state, toRemove).equals(NULL)) goingIn++;
        }
        return goingOut * goingIn;
    }

    /**
     * Perform a single step in the "Rip and Replace" procedure, removing the
     * provided state and editing all related connections to have the same
     * functionality without the ripped state.
     * @param toRip The state to remove. Must be within the set of states.
     */
    public void rip(State toRip) {
        if (!getStates().contains(toRip)) {
            throw new InvalidStateException("State '" + toRip + "' is not in GNFA!");
        }
        // s --> rip --> e
        // (s-rip)(rip*)(rip-e) U (s-e)
        Token selfLoop = transition(toRip, toRip);
        if (selfLoop instanceof NullToken) {
            selfLoop = new EmptyToken();
        } else if (!(selfLoop instanceof KleeneToken)) {
            selfLoop = new KleeneToken(selfLoop);
        }

        Set<State> states = getStates();

        // Programmatically, it's easier to do the repairing before the ripping.
        for (State start : states) {
            if (start.equals(toRip)) continue;
            Token goingIn = transition(start, toRip);
            if (goingIn instanceof NullToken) continue;

            for (State end : states) {
                if (end.equals(toRip)) continue;
                Token goingOut = transition(toRip, end);
                if (goingOut instanceof NullToken) continue;

                Token newPath = new ConcatToken(List.of(goingIn, selfLoop, goingOut));
                Token directPath = transition(start, end);
                if (directPath instanceof NullToken) {
                    setTransition(start, end, newPath);
                } else {
                    setTransition(start, end, new UnionToken(List.of(newPath, directPath)));
                }
            }
        }
        // The ripping.
        for (State state : states) {
            get(state).remove(toRip);
        }
        remove(toRip);
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
        if (size() == 0) return "{EMPTY}\n";
        Set<State> states = getStates();

        int longest = states.stream().mapToInt(x -> x.getName().length()).max().orElseThrow();
        if (longest < 6) longest = 6;
        longest++;
        String formatter = "%"+longest+"s";

        int lineLength = longest;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(formatter, "STATE"));
        for (State s : states) {
            sb.append(" |").append(String.format(formatter, s));
            lineLength += longest + 2;
        }
        sb.append('\n');
        sb.append("-".repeat(Math.max(0, lineLength)));
        sb.append('\n');

        states.stream().sorted().forEach(state -> {
            sb.append(String.format(formatter, state));
            // TODO: check what happens if the transition is longer
            //  than any input.
            for (State state2 : states) {
                String transitionStr = transition(state, state2).toString();
                sb.append(" |").append(String.format(formatter, transitionStr));
            }
            sb.append('\n');
        });

        return sb.toString();
    }
}
