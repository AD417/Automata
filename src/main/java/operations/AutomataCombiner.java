package operations;

import automata.DeterministicFiniteAutomaton;
import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import components.StateCombination;
import exception.AlphabetException;

import java.util.HashSet;
import java.util.Set;

public class AutomataCombiner {
    /**
     * Create an automaton that results from the intersection of the two
     * languages defined by DFAs.
     * This operation involves the cartesian product of the two automata, to
     * ensure that the states in both machines are accounted for at every
     * transition.
     * @param dfa1 The first DFA, whose language is intersected with the
     *             second DFA's language.
     * @param dfa2 The second DFA, whose language is intersected with the
     *             first DFA's language. Assumed to use the same language as
     *             the first one.
     * @return a DFA that accepts only strings that would be accepted by both
     * DFAs.
     */
    public static DeterministicFiniteAutomaton intersection(
            DeterministicFiniteAutomaton dfa1,
            DeterministicFiniteAutomaton dfa2
    ) {
        if (!dfa1.getAlphabet().equals(dfa2.getAlphabet())) {
            String msg = String.format(
                    "Alphabet mismatch: DFAs' alphabets (%s and %s) are incompatible.",
                    dfa1.getAlphabet(),
                    dfa2.getAlphabet()
            );
            throw new AlphabetException(msg);
        }
        StateCombination combination = StateCombination.createFor(dfa1.getStates(), dfa2.getStates());
        Alphabet alphabet = dfa1.getAlphabet();
        DeterministicTransition dtCombo = combineTransitions(
                dfa1.getTransitionFunction(),
                dfa2.getTransitionFunction(),
                alphabet,
                combination
        );

        State startCombo = combination.getCombo(dfa1.getStartState(), dfa2.getStartState());

        Set<State> acceptingCombo = new HashSet<>();
        for (State accept1 : dfa1.getAcceptingStates()) {
            for (State accept2 : dfa2.getAcceptingStates()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }

        return new DeterministicFiniteAutomaton(
                combination.getAllStates(),
                alphabet,
                dtCombo,
                startCombo,
                acceptingCombo
        );
    }

    /**
     * Create an automaton that results from the union of the two languages
     * defined by DFAs.
     * This operation involves the cartesian product of the two automata, to
     * ensure that the states in both machines are accounted for at every
     * transition.
     * @param dfa1 The first DFA, whose language is unioned with the second
     *             DFA's language.
     * @param dfa2 The second DFA, whose language is unioned with the first
     *             DFA's language. Assumed to use the same language as
     *             the first one.
     * @return a DFA that accepts only strings that would be accepted by either
     * DFAs.
     */
    public static DeterministicFiniteAutomaton union(
            DeterministicFiniteAutomaton dfa1,
            DeterministicFiniteAutomaton dfa2
    ) {
        if (!dfa1.getAlphabet().equals(dfa2.getAlphabet())) {
            throw new RuntimeException("Alphabet mismatch between two DFAs");
        }
        StateCombination combination = StateCombination.createFor(dfa1.getStates(), dfa2.getStates());
        Alphabet alphabet = dfa1.getAlphabet();
        DeterministicTransition dtCombo = combineTransitions(
                dfa1.getTransitionFunction(),
                dfa2.getTransitionFunction(),
                alphabet,
                combination
        );

        State startCombo = combination.getCombo(dfa1.getStartState(), dfa2.getStartState());

        Set<State> acceptingCombo = new HashSet<>();
        for (State accept1 : dfa1.getAcceptingStates()) {
            for (State accept2 : dfa2.getStates()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }
        for (State accept2 : dfa2.getAcceptingStates()) {
            for (State accept1 : dfa1.getStates()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }

        return new DeterministicFiniteAutomaton(
                combination.getAllStates(),
                alphabet,
                dtCombo,
                startCombo,
                acceptingCombo
        );
    }

    /**
     * Combine the transition functions of two DFAs via the cartesian product
     * method.
     * This ensures that the transitions and states of the associated DFAs are
     * simultaneously encoded in one transition function.
     * @param dt1 The transition function of the first DFA.
     * @param dt2 The transition function of the second DFA.
     * @param alphabet The alphabet used by both DFAs.
     * @param allStates A mapping function used to combine the states used in
     *                  both DFA transition functions.
     * @return a single DFA transition function that encodes both DFA
     * transitions.
     */
    private static DeterministicTransition combineTransitions(
            DeterministicTransition dt1,
            DeterministicTransition dt2,
            Alphabet alphabet,
            StateCombination allStates
    ) {
        DeterministicTransition dtCombo = new DeterministicTransition();
        dtCombo.setDefaults(allStates.getAllStates(), alphabet);
        for (State in1 : dt1.getStates()) {
            for (State in2 : dt2.getStates()) {
                State inCombo = allStates.getCombo(in1, in2);
                for (Character c : alphabet) {
                    State out1 = dt1.transition(in1, c);
                    State out2 = dt2.transition(in2, c);
                    State outCombo = allStates.getCombo(out1, out2);

                    dtCombo.setState(inCombo, c, outCombo);
                }
            }
        }
        return dtCombo;
    }

}
