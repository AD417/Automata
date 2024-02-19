package operations;

import automata.DeterministicFiniteAutomaton;
import automata.NondeterministicFiniteAutomaton;
import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import components.Transition;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class AutomataConvertor {
    /**
     * Convert a Deterministic Finite Automaton to a Nondeterministic
     * equivalent.
     * This is a trivial operation, performed by reassigning the deterministic
     * transition function as a nondeterministic variant with empty epsilon
     * transitions.
     * @param dfa the Deterministic Automaton to convert.
     * @return a Nondeterministic equivalent of the provided DFA. Any string
     * accepted by the DFA will be accepted by this NFA.
     */
    public static NondeterministicFiniteAutomaton DFAtoNFA(DeterministicFiniteAutomaton dfa) {
        Set<State> nfaStates = dfa.getStates();
        Alphabet alphabet = dfa .getAlphabet();
        Transition tf = new Transition();
        State startState = dfa.getStartState();
        Set<State> acceptingStates = dfa.getAcceptingStates();

        DeterministicTransition dt = dfa.getTransitionFunction();

        tf.initializeFor(nfaStates, alphabet);
        for (State state : nfaStates) {
            for (Character symbol : alphabet) {
                tf.setState(state, symbol, Set.of(dt.transition(state, symbol)));
            }
        }

        return new NondeterministicFiniteAutomaton(
                nfaStates,
                alphabet,
                tf,
                startState,
                acceptingStates
        );
    }


    /**
     * Convert a Nondeterministic Finite Automaton to a Deterministic
     * equivalent.
     * This is done by determining the set of states reachable by some number
     * of transitions.
     * @param nfa the Nondeterministic Automaton to convert.
     * @return a Deterministic equivalent of the provided NFA. Any string
     * accepted by the NFA will be accepted by this DFA.
     */
    public static DeterministicFiniteAutomaton NFAtoDFA(NondeterministicFiniteAutomaton nfa) {
        Set<State> dfaStates = new HashSet<>();
        Alphabet alphabet = nfa.getAlphabet();
        DeterministicTransition dt = new DeterministicTransition();
        State startState;
        Set<State> acceptingStates = new HashSet<>();

        Set<State> nfaStartClosure = nfa.epsilonClosure(nfa.getStartState());
        startState = subsetState(nfaStartClosure);
        if (wouldBeAccepted(nfa, nfaStartClosure)) {
            acceptingStates.add(startState);
        }

        Queue<Set<State>> statesToParse = new LinkedList<>();
        statesToParse.add(nfaStartClosure);

        while (!statesToParse.isEmpty()) {
            Set<State> subset = statesToParse.poll();
            State subsetState = subsetState(subset);

            if (dfaStates.contains(subsetState)) continue;
            dfaStates.add(subsetState);

            if (wouldBeAccepted(nfa, subset)) {
                acceptingStates.add(subsetState);
            }
            for (Character symbol : alphabet) {
                Set<State> outcomes = findAllOutcomes(nfa, subset, symbol);
                State outcomeSubsetState = subsetState(outcomes);
                statesToParse.add(outcomes);

                dt.setState(subsetState, symbol, outcomeSubsetState);

                if (wouldBeAccepted(nfa, outcomes)) acceptingStates.add(outcomeSubsetState);
            }
        }

        return new DeterministicFiniteAutomaton(
                dfaStates,
                alphabet,
                dt,
                startState,
                acceptingStates
        );
    }

    /**
     * Determine the name of a state that, effectively, encodes a subset of
     * NFA states. This name is equivalent to the list of states considered
     * listed in alphabetical order.
     * @param states the NFA states to consider.
     * @return a single state, with a name based on the other states.
     */
    private static State subsetState(Set<State> states) {
        StringBuilder sb = new StringBuilder("{");
        states.stream().sorted().forEach(state -> sb.append(state).append(", "));
        if (sb.lastIndexOf(",") != -1) {
            sb.replace(sb.lastIndexOf(","), sb.length(), "");
        }
        sb.append("}");
        return new State(sb.toString());
    }

    /**
     * Given a set of input states, determine all possible output states
     * reachable by transitioning across the given NFA with the given symbol.
     * @param nfa the NFA whose outcomes should be considered.
     * @param states the input states. Assumed to be a subset of the NFA's
     *               states.
     * @param symbol the input symbol. Assumed to be part of the NFA's
     *               alphabet.
     * @return all states reachable by taking (symbol) transitions from the
     * input states.
     */
    private static Set<State> findAllOutcomes(
            NondeterministicFiniteAutomaton nfa,
            Set<State> states,
            Character symbol
    ) {
        Set<State> reachable = new HashSet<>();
        states.stream()
                .map(state -> nfa.getTransitionFunction().transition(state, symbol))
                .map(nfa::epsilonClosure)
                .forEach(reachable::addAll);

        return reachable;
    }

    /**
     * Determine if any of this set of states would be accepted by the given
     * NFA. If this subset-state would be accepted on an NFA, then the subset-
     * state on a DFA would be accepted as well.
     * @param nfa the NFA whose outcomes should be considered.
     * @param states the input states. Assumed to be a subset of the NFA's
     *               states.
     * @return whether any of the states would be accepted.
     */
    private static boolean wouldBeAccepted(NondeterministicFiniteAutomaton nfa, Set<State> states) {
        return nfa.getAcceptingStates().stream().anyMatch(states::contains);
    }
}
