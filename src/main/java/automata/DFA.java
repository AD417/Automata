package automata;

import automata.components.Alphabet;
import automata.components.DeterministicTransition;
import automata.components.State;
import automata.components.StatePair;
import automata.exception.AlphabetException;
import automata.exception.InvalidAutomatonException;
import automata.exception.InvalidStateException;
import automata.operations.AutomataConvertor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic Finite Automaton: The most basic of the Language Recognition
 * Machines.
 * At all times, for all states, there is exactly one transition. The machine
 * may only be in exactly one state. The path taken by the machine is the same
 * so long as the input string is the same.
 * @param states The set of all states recognized by this DFA.
 * @param alphabet The set of all symbols that can be in strings read by this
 *                 DFA.
 * @param transitionFunction The transition function. Takes in a state and the
 *                           current symbol and outputs a single state to
 *                           transition to.
 * @param startState The state that the machine begins in.
 * @param acceptingStates The set of states that the machine may end up in
 *                        to accept the string; if the machine ends in any
 *                        other state, the string will be rejected.
 */
public record DFA(Set<State> states, Alphabet alphabet, DeterministicTransition transitionFunction, State startState,
                  Set<State> acceptingStates) {

    public DFA(
            Set<State> states,
            Alphabet alphabet,
            DeterministicTransition transitionFunction,
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

    /**
     * Validation function.
     * Checks if:
     * <ol>
     *     <li>The start state is in the state set</li>
     *     <li>All accepting states are in the state set</li>
     *     <li>
     *         The State set is closed under the transition function -- for any
     *         State q ∈ Q, d(q, s ∈ A) ∈ Q
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
                    "Invalid accepting state(s): %s are accepting states not in DFA set!",
                    missingStates
            );
            throw new InvalidAutomatonException(msg);
        }
        for (State state : states) {
            for (Character c : alphabet) {
                State result = transitionFunction.transition(state, c);
                if (result == null) {
                    String msg = String.format(
                            "Illegal transition: Transition δ(%s, '%c') is undefined",
                            state,
                            c
                    );
                    throw new InvalidAutomatonException(msg);
                }
                if (!states.contains(result)) {
                    String msg = String.format(
                            "Illegal transition: Transition δ(%s, '%c') is not closed (results in %s)",
                            state, c, result
                    );
                    throw new InvalidAutomatonException(msg);
                }
            }
        }
    }

    /**
     * Check if this DFA would accept this string -- If this string s ∈ L(DFA).
     * Mechanically, this means that the DFA, starting from the start state and
     * transitioning based on the symbols read from the string sequentially,
     * ends up in one of the accepting states.
     * @param string The string to check for acceptance.
     * @return true iff this string is accepted; false otherwise.
     */
    public boolean accepts(String string) {
        State state = startState;
        for (Character c : string.toCharArray()) {
            if (!alphabet.contains(c)) {
                String msg = String.format(
                        "String '%s' contains symbol '%c' not in Automaton's alphabet.",
                        string, c
                );
                throw new AlphabetException(msg);
            }
            State nextState = transitionFunction.transition(state, c);
            if (nextState == null) {
                String msg = String.format(
                        "Illegal transition: Transition δ(%s, %c) is undefined!",
                        state, c
                );
                throw new InvalidAutomatonException(msg);
            }
            if (!states.contains(nextState)) {
                String msg = String.format(
                        "Illegal transition: Transition δ(%s, %c) does not map to state set!",
                        state, c
                );
                throw new InvalidAutomatonException(msg);
            }
            state = nextState;
        }
        return acceptingStates.contains(state);
    }

    /**
     * Determine if two states within this DFA are distinguishable.
     * Mechanically, this means that there exists some string that, starting
     * from the given states, results in one start state accepting the string
     * and the other state rejecting the string.
     * @param first The first state to compare for distinguishabbility. Must be
     *              within the state set.
     * @param second The second state to compare for distinguishability. Must
     *               be within the state set.
     * @return true iff the states are distinguishable; false otherwise.
     */
    public boolean areDistinguishable(State first, State second) {
        // This algorithm is what I came up with when I first thought of the concept.
        // Needless to say, running this a bunch of times is pointless, but running it once...
        // I think it's pretty good, in isolation. I suck at doing multiple things
        // with one program.
        if (!states.contains(first)) {
            String msg = String.format("State %s is not in the state set!", first);
            throw new InvalidStateException(msg);
        }
        if (!states.contains(second)) {
            String msg = String.format("State %s is not in the state set!", second);
            throw new InvalidStateException(msg);
        }
        if (acceptingStates.contains(first) ^ acceptingStates.contains(second)) {
            // Trivial: empty string from either string is distinguishable.
            return true;
        }

        // Determine what states we end up
        HashMap<String, StatePair> stateCache = new HashMap<>();
        Queue<String> differs = new LinkedList<>();
        HashSet<StatePair> seen = new HashSet<>();

        stateCache.put("", new StatePair(first, second));
        seen.add(new StatePair(first, second));
        differs.offer("");

        while (!differs.isEmpty()) {
            String differ = differs.poll();

            StatePair pair = stateCache.get(differ);

            for (Character symbol : alphabet) {
                State firstStep = transitionFunction.transition(pair.first(), symbol);
                State secondStep = transitionFunction.transition(pair.second(), symbol);

                if (firstStep.equals(secondStep)) continue;

                // If there exists a string which is accepted by one, and not the other, then they are
                // distinguishable.
                if (acceptingStates.contains(firstStep) ^ acceptingStates.contains(secondStep)) {
                    return true;
                }

                StatePair nextPair = new StatePair(firstStep, secondStep);
                if (seen.contains(nextPair)) continue;
                seen.add(nextPair);

                String nextDiffer = differ + symbol;
                stateCache.put(nextDiffer, nextPair);
                differs.offer(nextDiffer);
            }
        }
        // Proof by exhaustion -- they are not distinguishable.
        return false;
    }

    private Set<StatePair> allStatePairs() {
        Set<StatePair> pairs = new HashSet<>();
        for (State first : states) {
            for (State second : states) {
                pairs.add(new StatePair(first, second));
            }
        }
        return pairs;
    }

    /**
     * Perform a single step in the "Determine all distinguishable pairs"
     * function.
     * @param unknownPairs Pairs whose distinguishable-ness are unknown.
     * @param distinguishable Pairs known to be distinguishable.
     * @return Pairs that can be proven to be distinguishable.
     */
    private Set<StatePair> indistinguishablePairStep(Set<StatePair> unknownPairs, Set<StatePair> distinguishable) {
        Set<StatePair> newDist = new HashSet<>();
        for (StatePair pair : unknownPairs) {
            if (distinguishable.contains(pair)) {
                newDist.add(pair);
                continue;
            }
            boolean allAreDist = true;
            for (Character symbol : alphabet) {
                State firstNext = transitionFunction.transition(pair.first(), symbol);
                State secondNext = transitionFunction.transition(pair.second(), symbol);

                StatePair pairNext = new StatePair(firstNext, secondNext);
                if (!distinguishable.contains(pairNext)) {
                    allAreDist = false;
                    break;
                }
            }

            if (allAreDist) newDist.add(pair);
        }
        return newDist;
    }

    /**
     * Determine every single pair of indistinguishable states in this DFA.
     * Indistinguishable states are defined as states where, starting from either
     * state and providing ANY string in the DFA's alphabet, both states agree
     * on whether the string will be accepted or rejected from that state.
     */
    private Set<StatePair> indistinguishablePairs() {
        Set<StatePair> unknownPairs = allStatePairs().stream()
                .filter(x -> !x.first().equals(x.second()))
                .collect(Collectors.toSet());

        Set<StatePair> distinguishable = unknownPairs.stream()
                .filter(x -> acceptingStates.contains(x.first()) ^ acceptingStates.contains(x.second()))
                .collect(Collectors.toSet());

        Set<StatePair> nowDistinguishable;

        do {
            nowDistinguishable = indistinguishablePairStep(unknownPairs, distinguishable);
            unknownPairs.removeAll(nowDistinguishable);
            distinguishable.addAll(nowDistinguishable);
        } while (!nowDistinguishable.isEmpty());

        return unknownPairs;
    }

    public Set<State> reachable() {
        Set<State> visitable = new HashSet<>();
        Queue<State> toVisit = new LinkedList<>();
        toVisit.add(startState);

        while (!toVisit.isEmpty()) {
            State parsing = toVisit.poll();
            if (visitable.contains(parsing)) continue;
            System.out.println(parsing);
            visitable.add(parsing);
            for (Character symbol : alphabet) {
                toVisit.add(transitionFunction.transition(parsing, symbol));
            }
        }
        return visitable;
    }

    /**
     * Create a copy of this DFA with all redundant or useless states removed.
     * Redundant states are defined according to the Myhill-Nerode Theorem
     * on DFA state distinguishability.
     * @return a copy of this DFA with states removed and the transition
     * function modified to ensure the number of states is as small
     * as possible.
     */
    public DFA minify() {
        Set<State> reached = reachable();
        Set<StatePair> notDistPairs = indistinguishablePairs();

        DeterministicTransition tf = new DeterministicTransition();
        tf.setDefaults(reached, alphabet);
        tf.copyFrom(transitionFunction, alphabet);

        for (StatePair pair : notDistPairs) {
            // If either thing in the pair is unreachable, ignore it.
            if (!reached.contains(pair.first()) || !reached.contains(pair.second())) continue;
            for (State now : reached) {
                for (Character symbol : alphabet) {
                    if (tf.transition(now, symbol).equals(pair.first())) {
                        // Redirect everything from first to second.
                        tf.setState(now, symbol, pair.second());
                    }
                }
            }
            // Drop back and punt indistinguishable states.
            tf.remove(pair.first());
            reached.remove(pair.first());
        }

        Set<State> accepted = new HashSet<>(acceptingStates);
        accepted.retainAll(reached);

        // TODO: what happens if startState is minified out?
        return new DFA(reached, alphabet, tf, startState, accepted);
    }

    /**
     * Create a clone of this DFA, with every single state renamed to
     * something else.
     * This is a workaround for combining a DFA with itself. Under most
     * circumstances (DFA U DFA), this is pointless, but with concatenation
     * this is something reasonable.
     * @return a copy of this DFA with an otherwise identical state diagram,
     * but all the states are renamed.
     */
    public DFA cloneReplaceStates() {
        HashMap<State, State> stateMap = new HashMap<>();
        states.stream()
                .map(state -> Map.entry(state, new State()))
                .forEach(x -> stateMap.put(x.getKey(), x.getValue()));

        Set<State> newStates = new HashSet<>(stateMap.values());
        DeterministicTransition transition = new DeterministicTransition();
        State newStart = stateMap.get(startState);
        Set<State> newAccept = acceptingStates.stream().map(stateMap::get).collect(Collectors.toSet());

        for (State state : states) {
            for (Character symbol : alphabet) {
                transition.setState(
                        stateMap.get(state),
                        symbol,
                        stateMap.get(transitionFunction.transition(state, symbol))
                );
            }
        }

        return new DFA(newStates, alphabet, transition, newStart, newAccept);
    }

    /**
     * Convert this DFA to a NFA, proving the equivalence of DFAs and NFAs.
     * @return an equivalent NFA: L(NFA) == L(DFA)
     * @see AutomataConvertor#DFAtoNFA
     */
    public NFA toNFA() {
        return AutomataConvertor.DFAtoNFA(this);
    }

    /**
     * Convert this DFA to a GNFA for conversion to a Regex String.
     * @return a GNFA. L(GNFA) == L(DFA).
     */
    public GNFA toGNFA() {
        return toNFA().toGNFA();
    }

    @Override
    public String toString() {
        return "DFA D = (Q, Σ, δ, q0, F), where:\n" +
                "Q = " + states + "\n" +
                "Σ = " + alphabet + "\n" +
                "δ = the following table:\n" + transitionFunction +
                "q0 = " + startState + "\n" +
                "F = " + acceptingStates;
    }
}
