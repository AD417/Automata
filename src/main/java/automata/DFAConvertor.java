package automata;

import components.Alphabet;
import components.DeterministicTransition;
import components.State;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DFAConvertor {
    /**
     * Convert a Nondeterministic Finite Automaton to a Deterministic
     * equivalent.
     * This is done by determining the set of states reachable by some number
     * of transitions.
     * @param nfa the Nondeterministic Automaton to convert.
     * @return a Deterministic equivalent of the provided NFA. Any string
     * accepted by the NFA will be accepted by this DFA.
     */
    public static DeterministicFiniteAutomaton convertNFA(NondeterministicFiniteAutomaton nfa) {
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

    private static State subsetState(Set<State> states) {
        StringBuilder sb = new StringBuilder("{ ");
        states.stream().sorted().forEach(state -> sb.append(state.getName()).append(", "));
        sb.append("}");
        return new State(sb.toString());
    }

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

    private static boolean wouldBeAccepted(NondeterministicFiniteAutomaton nfa, Set<State> states) {
        return nfa.getAcceptingStates().stream().anyMatch(states::contains);
    }
}
