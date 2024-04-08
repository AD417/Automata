package automata;

import automata.components.*;
import automata.exception.AlphabetException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A PushDown Automaton (PDA). Has comparable functionality to a {@link NFA},
 * but includes more complex state manipulation to allow it to recognize a
 * larger set of languages. <br>
 * The main addition is a stack, a basic data structure allowing elements to
 * be stored in a "Last In First Out" manner. The PDA can access the most
 * recently pushed element on the stack, but must remove that element to
 * access elements below it, if valid. <br>
 * PDAs are able to encode any {@link grammar.CFG Context-Free Grammar}.
 * @param states The set of all states.
 * @param stringAlphabet The set of all symbols that can be read from the input
 *                       string.
 * @param stackAlphabet The set of all symbols that can be pushed onto the
 *                      PDA stack.
 * @param relation The transition function. Takes in the current state, the
 *                 current symbol in the parsed string (or nothing), and the
 *                 most recently popped element from the stack (or nothing) to
 *                 determine the next state to go to and the next symbol to
 *                 push to the stack.
 * @param startState The state the PDA starts in. Must be in the state set.
 * @param acceptingStates The states that allow a PDA to accept a string if it
 *                        is possible for a set of moves to end up in one of
 *                        them. Must be a subset of the state set.
 * @implNote some interpretations of the PDA's stack may include
 * a "starting symbol". This implementation does not; to add a starting
 * stack symbol, encode a start state in the relation that pushes the start
 * symbol to the stack before continuing to the actual start of the PDA.
 * Additionally, since it is possible for the PDA to get stuck in a loop of
 * epsilon transitions that results in an infinite number of elements being
 * pushed to the stack, it is assumed that a maximum of |Q| epsilon transitions
 * may occur between reads from the tape head. This may be an underestimate in
 * some cases, particularly when a PDA is created from a CFG.
 */
public record PDA(Set<State> states,
                  Alphabet stringAlphabet,
                  StackAlphabet stackAlphabet,
                  StackTransition relation,
                  State startState,
                  Set<State> acceptingStates) {

    /**
     * An internal hashable representation of the "true state" of a PDA at
     * some time. Includes not only the current machine state, but the contents
     * of the stack.
     * @param state The current machine state.
     * @param stack The contents of the stack.
     */
    private record PDAConfiguration(State state, Stack<String> stack) {}

    /**
     * The starting configuration, including the empty stack.
     * @return a configuration including the start state and empty stack.
     */
    private Set<PDAConfiguration> startConfig() {
        return epsilonClosure(new PDAConfiguration(startState, new Stack<>()));
    }

    // Lazy method to clone and push/pop from the stack.
    private Stack<String> nextStack(Stack<String> current, String symbol, boolean isEpsilon) {
        @SuppressWarnings("unchecked")
        Stack<String> next = (Stack<String>) current.clone();
        if (!isEpsilon) next.pop();
        if (!Objects.equals(symbol, "" + Alphabet.EPSILON)) {
            next.push(symbol);
        }
        return next;
    }

    /**
     * Perform a single transition for a given state, where you read the given
     * symbol.
     * @param config The configuration to transition.
     * @param symbol The symbol the PDA read. May be epsilon.
     * @return The set of all configurations reachable from
     * this current configuration.
     */
    private Set<PDAConfiguration> transitionStep(PDAConfiguration config, Character symbol) {
        Set<PDAConfiguration> step = new HashSet<>();
        if (symbol == Alphabet.EPSILON) step.add(config);

        for (StackState eResult : relation.epsilonStackTransition(config.state, symbol)) {
            //System.out.println(config.state + ", ε, " + symbol + " --> " + eResult);
            Stack<String> futureStack = nextStack(config.stack, eResult.stackSymbol(), true);
            step.add(new PDAConfiguration(eResult.state(), futureStack));
        }
        if (!config.stack.isEmpty()) {
            StackState input = new StackState(config.state, config.stack.peek());
            for (StackState result : relation.transition(input, symbol)) {
                //System.out.println(input + ", " + symbol + " --> " + result);
                Stack<String> futureStack = nextStack(config.stack, result.stackSymbol(), false);
                step.add(new PDAConfiguration(result.state(), futureStack));
            }
        }
        return step;
    }

    /**
     * Compute the Epsilon Closure of a configuration. This is an
     * operation that determines the set of all reachable states and stack
     * configurations from the input states via epsilon transitions alone.
     * @param config A configuration to determine the epsilon closure of.
     * @return The set of configs that can be reached via a reasonable number
     * of epsilon transitions.
     * @implNote "A reasonable number" of epsilon transitions is defined as the
     * number of states in the PDA. This may not be a sufficient amount for
     * some applications, such as a CFG-derived PDA.
     */
    private Set<PDAConfiguration> epsilonClosure(PDAConfiguration config) {
        Set<PDAConfiguration> out = new HashSet<>();
        out.add(config);

        Set<PDAConfiguration> step = new HashSet<>();
        step.add(config);
        Set<PDAConfiguration> nextStep = new HashSet<>();

        // NOTE:
        //  This is a very hacky solution to ensure that the PDA never gets
        //  stuck in a loop while actually being able to do the epsilon
        //  transitions it needs to. It will do as many epsilon transitions
        //  as there are states in the machine. Arguably, this is also
        //  insufficient, but you can increase the states factor if necessary.
        for (int p = 0; p <= states().size(); p++) {
            nextStep = step.stream()
                    //.filter(out::contains)
                    .map(x -> transitionStep(x, Alphabet.EPSILON))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            out.addAll(step);
            step = nextStep;
        }
        out.addAll(nextStep);
        return out;
    }

    /**
     * Determine if a string is within the context-free language defined by
     * this PDA. More specifically, determines if there is any possible set of
     * steps that the PDA can follow to end up in an accept state.
     * @param string the string to test with this PDA. The string parsing
     *               begins with at the first character and runs through the
     *               entire string.
     * @return True iff the PDA can end up in an accept state after parsing
     * every character in the string; false otherwise.
     */
    public boolean accepts(String string) {
        Set<PDAConfiguration> currentConfigs = startConfig();

        // currentConfigs.forEach(System.out::println);

        for (Character symbol : string.toCharArray()) {
            if (!stringAlphabet.contains(symbol)) {
                String msg = String.format(
                        "String '%s' contains symbol '%c' not in Automaton's alphabet.",
                        string, symbol);
                throw new AlphabetException(msg);
            }
            currentConfigs = currentConfigs.stream()
                    .map(c -> transitionStep(c, symbol))
                    .flatMap(Set::stream)
                    .map(this::epsilonClosure)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
        }
        return currentConfigs.stream().anyMatch(x -> acceptingStates.contains(x.state));
    }

    @Override
    public String toString() {
        return "PDA P = (Q, Σ, Γ, δ, q0, F), where:\n" +
                "Q = " + states + "\n" +
                "Σ = " + stringAlphabet + "\n" +
                "Γ = " + stackAlphabet + "\n" +
                "δ = the following relation data:\n" + relation +
                "q0 = " + startState + "\n" +
                "F = " + acceptingStates;

    }
}
