package automata;

import automata.components.*;
import automata.exception.AlphabetException;
import automata.exception.InvalidAutomatonException;
import automata.operations.AutomataConvertor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Nondeterministic Finite Automaton: a more complex Language Recognition
 * Machine than a DFA, but providing more potential outcomes. <br>
 * A NFA has the following differences from a DFA:
 * <ol>
 * <li>
 *     When an NFA reads a symbol, it may have multiple choices for where it
 *     will go according to its transition function, or even none at all.
 *     The NFA can choose any of these choices, but must choose one,
 *     if available.
 * </li>
 * <li>
 *     If an NFA reaches a point where it would have no valid transition out of
 *     its state, it is considered to immediately reject the string
 *     it is parsing.
 * </li>
 * <li>
 *     The current state of the NFA can change without reading a symbol from
 *     the tape head. This is known as an "epsilon transition". The set of all
 *     states reachable from an input state via epsilon transitions is known as
 *     the "epsilon closure" of that state.
 * </li>
 * <li>
 *     If there is any way for a string to be accepted through careful
 *     selection of transitions (or, more likely, trying every single state),
 *     then the NFA accepts the string and it is part of the language
 *     represented by the machine.
 * </li>
 * </ol>
 * @param states The set of all states recognized by this NFA.
 * @param alphabet The set of all symbols that can be in strings read by this
 *                 NFA.
 * @param transitionFunction The transition function. Takes in a state and the
 *                           current symbol and outputs the set of all states
 *                           that may be transitioned to. Includes epsilon
 *                           transitions.
 * @param startState The state that the machine begins in.
 * @param acceptingStates The set of states that the machine may end up in
 *                        to accept the string; if there is no set of moves
 *                        that can lead to an accepting state, then the string
 *                        is immediately rejected.
 */
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

    /**
     * Validation function.
     * Checks if:
     * <ol>
     *     <li>The start state is in the state set</li>
     *     <li>All accepting states are in the state set</li>
     *     <li>
     *         The State set maps to its power set via the transition function:
     *         for any State q ∈ Q, d(q, s ∈ A) ∈ P(Q)
     *     </li>
     * </ol>
     */
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

    /**
     * Determine if a string is within the regular language defined by this
     * NFA. More specifically, determines if there is any possible set of steps
     * that the NFA can follow to end up in an accept state.
     * @param string the string to test with this NFA. The string parsing
     *               begins with at the first character and runs through the
     *               entire string.
     * @return True iff the NFA can end up in an accept state after parsing
     * every character in the string; false otherwise.
     */
    public boolean accepts(String string) {
        Set<State> currentStates = epsilonClosure(startState);

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
    /**
     * Determine the valid ending points for acceptable substrings of the
     * provided string, assuming parsing begins at the specified starting
     * point.
     * @param string the string to parse.
     * @return a list of all possible ending positions for this string.
     * These integers are such that `string.substring(0, x) will return a
     * string that will be accepted by this NFA. These values are in ascending
     * order.
     */
    public List<Integer> acceptableSubstrings(String string) {
        return acceptableSubstrings(string, 0);
    }

    /**
     * Determine the valid ending points for acceptable substrings of the
     * provided string, assuming parsing begins at the specified starting
     * point.
     * @param string the string to parse.
     * @param start the index to begin parsing at. Must be between 0 inclusive
     *              and the length of the string exclusive.
     * @return a list of all possible ending positions for this string.
     * These integers are such that `string.substring(start, x) will return a
     * string that will be accepted by this NFA. These values are in ascending
     * order.
     */
    public List<Integer> acceptableSubstrings(String string, int start) {
        List<Integer> valid = new LinkedList<>();
        Set<State> currentStates = epsilonClosure(startState);
        for (int pos = start; pos < string.length(); pos++) {
            Character symbol = string.charAt(pos);
            if (!alphabet.contains(symbol)) {
                String msg = String.format(
                        "String '%s' contains symbol '%c' not in Automaton's alphabet.",
                        string, symbol
                );
                throw new AlphabetException(msg);
            }

            Set<State> immediateNextStates = new HashSet<>();
            currentStates.stream()
                    .map(state -> transitionFunction.transition(state, symbol))
                    .filter(Objects::nonNull)
                    .forEach(immediateNextStates::addAll);

            currentStates = epsilonClosure(immediateNextStates);

            if (acceptingStates.stream().anyMatch(currentStates::contains)) {
                valid.add(pos+1);
            }
        }
        return valid;
    }

    /**
     * Compute the Epsilon closure of a state.
     * @param state The state to find the epsilon closure of.
     * @return The set of states that can be reached from this state without
     * reading from the tapehead.
     * @see #epsilonClosure(Set)
     */
    public Set<State> epsilonClosure(State state) {
        return epsilonClosure(Set.of(state));
    }

    /**
     * Compute the Epsilon Closure of a set of states. This is an operation
     * from q ∈ P(Q) unto itself, determining the set of all states reachable
     * from the input states via epsilon transitions alone.
     * @param states the set of states to check epsilon transitions from.
     * @return The set of states that can be reached via epsilon transitions.
     */
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

    /**
     * Create a clone of this NFA, with every single state renamed to
     * something else.
     * This is a workaround for combining a NFA with itself. Under most
     * circumstances (NFA U NFA), this is pointless, but with concatenation
     * this is something reasonable.
     * @return a copy of this NFA with an otherwise identical state diagram,
     * but all the states are renamed.
     */
    public NFA cloneReplaceStates() {
        HashMap<State, State> stateMap = new HashMap<>();
        states.stream()
                .map(state -> Map.entry(state, new State()))
                .forEach(x -> stateMap.put(x.getKey(), x.getValue()));

        Set<State> newStates = new HashSet<>(stateMap.values());
        Transition transition = new Transition();
        State newStart = stateMap.get(startState);
        Set<State> newAccept = acceptingStates.stream().map(stateMap::get).collect(Collectors.toSet());

        transition.initializeFor(newStates, alphabet);
        for (State state : states) {
            for (Character symbol : alphabet) {
                Set<State> newResult = transitionFunction.transition(state, symbol).stream()
                        .map(stateMap::get)
                        .collect(Collectors.toSet());
                transition.setState(stateMap.get(state), symbol, newResult);
            }
            Set<State> newResult = transitionFunction.transition(state, Alphabet.EPSILON).stream()
                    .map(stateMap::get)
                    .collect(Collectors.toSet());
            transition.setState(stateMap.get(state), Alphabet.EPSILON, newResult);
        }

        return new NFA(newStates, alphabet, transition, newStart, newAccept);
    }

    /**
     * Create a clone of this NFA, with redundant states removed.
     * Redundant states are defined as any states that only serves to provide
     * epsilon transitions to other states. This definition excludes states
     * that serve as an initial or final state.
     * @return a clone of this NFA with any states that have only epsilon
     * transitions removed.
     */
    public NFA simplifyEpsilon() {
        HashMap<State, Set<State>> redundantEpsilons = new HashMap<>();
        Set<State> kept = new HashSet<>();
        for (State state : states) {
            // Starting and accepting states that are only epsilon transitions usually serve
            if (state.equals(startState) || acceptingStates.contains(state)) {
                kept.add(state);
                continue;
            }
            boolean isRedundant = true;
            for (Character symbol : alphabet) {
                Set<State> result = transitionFunction.transition(state, symbol);
                if (Objects.isNull(result) || result.isEmpty()) continue;
                isRedundant = false;
                break;
            }
            Set<State> epsilonResult = epsilonClosure(state);
            // Trap states, which would have no outputs (including epsilons), are ignored.
            if (!isRedundant || epsilonResult.isEmpty()) {
                kept.add(state);
                continue;
            }

            redundantEpsilons.put(state, epsilonResult);
        }

        // Remove chains: if A -> B -> C -> D, simplify to A -> D
        for (Set<State> redundantOut : redundantEpsilons.values()) {
            for (State redundantIn : redundantEpsilons.keySet()) {
                if (redundantOut.contains(redundantIn)) {
                    redundantOut.addAll(redundantEpsilons.get(redundantIn));
                }
            }
            redundantOut.removeAll(redundantEpsilons.keySet());
        }

        Transition tf = new Transition();
        tf.initializeFor(kept, alphabet);

        Set<Character> trueAlphabet = new HashSet<>(alphabet);
        // Start state can still have epsilons!
        trueAlphabet.add(Alphabet.EPSILON);
        for (State state : kept) {
            for (Character symbol : trueAlphabet) {
                Set<State> result = new HashSet<>(transitionFunction.transition(state, symbol));
                Set<State> redundant = result.stream()
                        .filter(redundantEpsilons::containsKey)
                        .collect(Collectors.toSet());

                result.removeAll(redundant);
                redundant.forEach(toFix -> result.addAll(redundantEpsilons.get(toFix)));

                tf.setState(state, symbol, result);
            }
        }
        return new NFA(kept, alphabet, tf, startState, acceptingStates);
    }

    /**
     * Reduce the "complexity" of the NFA by ensuring that the starting and
     * ending states have no transitions out of them.
     * Since we're lazy, we create a new first state that epsilon-transitions
     * to the original start state, and a new accept state that all original
     * accept states epsilon-transition to.
     * @return a new NFA with a start state with no transitions to, and a
     * singular end state that doesn't transition anywhere.
     */
    public NFA reduceStartEndComplexity() {
        State start = new State();
        State end = new State();

        Set<State> nextStates = new HashSet<>(states);
        nextStates.add(start);
        nextStates.add(end);

        Transition tf = new Transition();
        tf.initializeFor(nextStates, alphabet);
        transitionFunction.addAllTo(tf);
        tf.setState(start, Alphabet.EPSILON, startState);
        for (State s : acceptingStates) tf.setState(s, Alphabet.EPSILON, end);

        return new NFA(nextStates, alphabet, tf, start, Set.of(end));
    }

    /**
     * Convert this NFA to a DFA, proving the equivalence of DFAs and NFAs.
     * @return an equivalent DFA: L(DFA) == L(NFA)
     * @see AutomataConvertor#NFAtoDFA
     */
    public DFA toDFA() {
        return AutomataConvertor.NFAtoDFA(this);
    }

    /**
     * Convert this NFA to a GNFA for conversion to a Regex String.
     * This is a trivial operation, since the resulting transition strings
     * are all either single characters, epsilons, or empty sets, and map
     * exactly from the NFA's transition function.
     * @return a GNFA. L(GNFA) == L(NFA).
     */
    public GNFA toGNFA() {
        NFA copy = reduceStartEndComplexity();
        GeneralTransition gt = new GeneralTransition();
        gt.convertFrom(copy.transitionFunction, copy.alphabet);

        State end = copy.acceptingStates.stream().findFirst().orElseThrow();

        return new GNFA(copy.states, copy.alphabet, gt, copy.startState, end);
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
