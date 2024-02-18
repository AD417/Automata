package automata;

import components.Alphabet;
import components.DeterministicTransition;
import components.State;
import components.StateCombination;

import java.util.HashSet;
import java.util.Set;

public class AutomataCombiner {

    public static DeterministicFiniteAutomaton intersection(
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
