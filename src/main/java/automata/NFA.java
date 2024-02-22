package automata;

import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import components.Transition;
import exception.AlphabetException;
import exception.InvalidAutomatonException;

import java.util.*;
import java.util.stream.Collectors;

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

    public Set<State> epsilonClosure(State state) {
        return epsilonClosure(Set.of(state));
    }

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
