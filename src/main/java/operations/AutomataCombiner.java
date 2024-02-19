package operations;

import automata.DFA;
import automata.NFA;
import components.*;
import exception.AlphabetException;
import exception.InvalidStateException;

import java.util.HashSet;
import java.util.Set;

public class AutomataCombiner {

    public static NFA concatenate(
            NFA nfa1,
            NFA nfa2
    ) {
        // TODO: the overuse of this alphabet guard is getting annoying.
        if (!nfa1.alphabet().equals(nfa2.alphabet())) {
            String msg = String.format(
                    "Alphabet mismatch: DFAs' alphabets (%s and %s) are incompatible.",
                    nfa1.alphabet(),
                    nfa2.alphabet()
            );
            throw new AlphabetException(msg);
        }

        Set<State> allStates = new HashSet<>();
        Alphabet alphabet = nfa1.alphabet();
        Transition tf = new Transition();
        State start = nfa1.startState();
        Set<State> acceptingStates = nfa2.acceptingStates();

        allStates.addAll(nfa1.states());
        allStates.addAll(nfa2.states());

        int expectedStateSize = nfa1.states().size() + nfa2.states().size();
        if (allStates.size() < expectedStateSize) {
            Set<State> stateOverlap = new HashSet<>(nfa1.states());
            stateOverlap.retainAll(nfa2.states());
            String msg = String.format("States %s are present in both NFAs.", stateOverlap);
            throw new InvalidStateException(msg);
        }

        tf.initializeFor(allStates, alphabet);
        nfa1.transitionFunction().addAllTo(tf);
        nfa2.transitionFunction().addAllTo(tf);
        for (State firstAccept : nfa1.acceptingStates()) {
            Set<State> epsilonTransition = new HashSet<>(
                    tf.transition(firstAccept, Alphabet.EPSILON)
            );
            epsilonTransition.add(nfa2.startState());
            tf.setState(firstAccept, Alphabet.EPSILON, epsilonTransition);
        }

        return new NFA(allStates, alphabet, tf, start, acceptingStates);
    }

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
    public static DFA intersection(DFA dfa1, DFA dfa2) {
        if (!dfa1.alphabet().equals(dfa2.alphabet())) {
            String msg = String.format(
                    "Alphabet mismatch: DFAs' alphabets (%s and %s) are incompatible.",
                    dfa1.alphabet(),
                    dfa2.alphabet()
            );
            throw new AlphabetException(msg);
        }
        StateCombination combination = StateCombination.createFor(dfa1.states(), dfa2.states());
        Alphabet alphabet = dfa1.alphabet();
        DeterministicTransition dtCombo = combineTransitions(
                dfa1.transitionFunction(),
                dfa2.transitionFunction(),
                alphabet,
                combination
        );

        State startCombo = combination.getCombo(dfa1.startState(), dfa2.startState());

        Set<State> acceptingCombo = new HashSet<>();
        for (State accept1 : dfa1.acceptingStates()) {
            for (State accept2 : dfa2.acceptingStates()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }

        return new DFA(combination.getAllStates(), alphabet, dtCombo, startCombo, acceptingCombo);
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
    public static DFA union(DFA dfa1, DFA dfa2) {
        if (!dfa1.alphabet().equals(dfa2.alphabet())) {
            String msg = String.format(
                    "Alphabet mismatch: DFAs' alphabets (%s and %s) are incompatible.",
                    dfa1.alphabet(),
                    dfa2.alphabet()
            );
            throw new AlphabetException(msg);
        }
        StateCombination combination = StateCombination.createFor(dfa1.states(), dfa2.states());
        Alphabet alphabet = dfa1.alphabet();
        DeterministicTransition dtCombo = combineTransitions(
                dfa1.transitionFunction(),
                dfa2.transitionFunction(),
                alphabet,
                combination
        );

        State startCombo = combination.getCombo(dfa1.startState(), dfa2.startState());

        Set<State> acceptingCombo = new HashSet<>();
        for (State accept1 : dfa1.acceptingStates()) {
            for (State accept2 : dfa2.states()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }
        for (State accept2 : dfa2.acceptingStates()) {
            for (State accept1 : dfa1.states()) {
                acceptingCombo.add(combination.getCombo(accept1, accept2));
            }
        }

        return new DFA(combination.getAllStates(), alphabet, dtCombo, startCombo, acceptingCombo);
    }

    public static NFA union(NFA nfa1, NFA nfa2) {
        if (!nfa1.alphabet().equals(nfa2.alphabet())) {
            String msg = String.format(
                    "Alphabet mismatch: DFAs' alphabets (%s and %s) are incompatible.",
                    nfa1.alphabet(),
                    nfa2.alphabet()
            );
            throw new AlphabetException(msg);
        }
        Set<State> allStates = new HashSet<>();
        Alphabet alphabet = nfa1.alphabet();
        Transition tf = new Transition();
        State commonStart = new State();
        Set<State> acceptingStates = new HashSet<>();

        allStates.addAll(nfa1.states());
        allStates.addAll(nfa2.states());
        allStates.add(commonStart);

        int expectedStateSize = nfa1.states().size() + nfa2.states().size() + 1;
        if (allStates.size() < expectedStateSize) {
            Set<State> stateOverlap = new HashSet<>(nfa1.states());
            stateOverlap.retainAll(nfa2.states());
            String msg = String.format("States %s are present in both NFAs.", stateOverlap);
            throw new InvalidStateException(msg);
        }

        tf.initializeFor(allStates, alphabet);
        nfa1.transitionFunction().addAllTo(tf);
        nfa2.transitionFunction().addAllTo(tf);
        Set<State> commonStartTransitions = Set.of(nfa1.startState(), nfa2.startState());
        tf.setState(commonStart, Alphabet.EPSILON, commonStartTransitions);

        acceptingStates.addAll(nfa1.acceptingStates());
        acceptingStates.addAll(nfa2.acceptingStates());

        return new NFA(allStates, alphabet, tf, commonStart, acceptingStates);
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
