package operations;

import automata.DFA;
import automata.NFA;
import components.*;
import exception.AlphabetException;
import exception.InvalidAutomatonException;
import exception.InvalidStateException;

import java.util.HashSet;
import java.util.Set;

public class AutomataCombiner {
    /**
     * Construct an automaton that can concatenate the two languages defined by
     * two NFAs.
     * This operation involves pointing all the first NFA's accept states at
     * the second NFA's starting state via epsilon transitions. Once part of a
     * word accepted by the first NFA is found, the second NFA can assert the
     * remainder of the string is accepted as well.
     * @param nfa1 The first NFA. Must come first in the concatenation.
     * @param nfa2 The second NFA. Must come second in the concatenation.
     *             Assumed to use the same alphabet as the first NFA.
     * @return a single NFA that encodes a language defined as the
     * concatenation of the languages defined by the input NFAs.
     */
    public static NFA concatenate(NFA nfa1, NFA nfa2) {
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
     * Construct an automaton that results from the intersection of the two
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
     * Construct an automaton that results from applying a Kleene Star
     * operation to the langauge defined by an NFA.
     * This means that strings from the language can occur any number of times,
     * including zero. The NFA just loops back on itself, with accepting states
     * having epsilon transitions back to the start.
     * @param nfa the NFA to apply the Kleene Star operation to.
     * @return a NFA that accepts any string in the set defined by taking the
     * kleene star of the language defined by the input NFA.
     */
    public static NFA kleeneStar(NFA nfa) {
        Set<State> states = new HashSet<>(nfa.states());
        Alphabet alphabet = nfa.alphabet();
        Transition tf = new Transition();
        State start = new State();
        Set<State> accepting = Set.of(start);

        states.add(start);

        tf.initializeFor(states, alphabet);
        nfa.transitionFunction().addAllTo(tf);
        tf.setState(start, Alphabet.EPSILON, nfa.startState());

        for (State lastState : nfa.acceptingStates()) {
            Set<State> lastEpsilon = new HashSet<>(tf.transition(lastState, Alphabet.EPSILON));
            lastEpsilon.add(start);
            tf.setState(lastState, Alphabet.EPSILON, lastEpsilon);
        }

        return new NFA(states, alphabet, tf, start, accepting);
    }

    /**
     * Construct an automaton that can raise a language to a specified power.
     * This operation involves concatenating the language <c>power</c> times,
     * applying the relevant operation to the associated NFA.
     * @param nfa the NFA whose language is being raised to a power.
     * @param power The number of times to concatenate the language. Must be
     *              at least 1.
     * @return An NFA that encodes a language defined as the concatenation of
     * the input NFA's language <c>power</c> times.
     */
    public static NFA power(NFA nfa, int power) {
        if (power < 1) {
            String msg = String.format("Cannot construct automaton for N^%d; power must be at least 1.", power);
            throw new InvalidAutomatonException(msg);
        }

        NFA out = nfa.cloneReplaceStates();

        for (int i = 1; i < power; i++) {
            out = concatenate(out, nfa.cloneReplaceStates());
        }
        return out;
    }

    /**
     * Construct an automaton that results from the union of the two languages
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

    /**
     * Construct an automaton that results from the union of the two languages
     * defined by NFAs.
     * This operation uses the non-deterministic nature of NFAs, utilizing the
     * ability of an NFA to take multiple paths to find an accepting state in
     * either automaton, if possible. This does not work with intersection,
     * sadly.
     * @param nfa1 The first NFA, whose language is unioned with the second
     *             NFA's language.
     * @param nfa2 The second NFA, whose language is unioned with the first
     *             NFA's language. Assumed to use the same language as
     *             the first one.
     * @return a NFA that accepts only strings that would be accepted by either
     * NFAs.
     */
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
