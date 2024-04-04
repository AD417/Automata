package automata;

import automata.components.*;
import automata.exception.AlphabetException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A PushDown Automaton (PDA). Has identical functionality to a {@link NFA},
 * but includes a stack that may be manipulated to encode more complex
 * languages. PDAs are able to encode any
 * {@link grammar.CFG Context-Free Grammar}.
 * This PDA starts with an empty stack; there is no "starting symbol". To add
 * a starting symbol, encode a start state in the relation that pushes a start
 * symbol to the stack before going to the actual start of the PDA.
 * @param states The set of all states.
 * @param stringAlphabet The set of all symbols that can appear in the input
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
 */
public record PDA(Set<State> states,
                  Alphabet stringAlphabet,
                  StackAlphabet stackAlphabet,
                  StackTransition relation,
                  State startState,
                  Set<State> acceptingStates) {

    private record PDAConfiguration(State state, Stack<String> stack) {}

    /**
     * The starting configuration, including the stack.
     * @return a configuration including the start state and empty stack.
     */
    private Set<PDAConfiguration> startConfig() {
        return epsilonClosure(new PDAConfiguration(startState, new Stack<>()));
    }

    private Stack<String> nextStack(Stack<String> current, String symbol, boolean isEpsilon) {
        @SuppressWarnings("unchecked")
        Stack<String> next = (Stack<String>) current.clone();
        if (!isEpsilon) next.pop();
        if (!Objects.equals(symbol, "" + Alphabet.EPSILON)) {
            next.push(symbol);
        }
        return next;
    }

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

        /*Set<PDAConfiguration> epsilonStep = new HashSet<>(step);

        for (PDAConfiguration pda : step) {
            for (StackState result : relation.epsilonStackTransition(config.state, symbol)) {
                Stack<String> futureStack = nextStack(config.stack, result.stackSymbol(), true);
                epsilonStep.add(new PDAConfiguration(result.state(), futureStack));
            }
            if (!pda.stack.isEmpty()) {
                StackState input = new StackState(pda.state, pda.stack.peek());
                for (StackState result : relation.transition(input, symbol)) {
                    Stack<String> futureStack = nextStack(pda.stack, result.stackSymbol(), false);
                    epsilonStep.add(new PDAConfiguration(result.state(), futureStack));
                }
            }
        }
        return epsilonStep;*/
    }

    private Set<PDAConfiguration> epsilonClosure(PDAConfiguration config) {
        Set<PDAConfiguration> out = new HashSet<>();
        out.add(config);

        Set<PDAConfiguration> step = new HashSet<>();
        step.add(config);
        Set<PDAConfiguration> nextStep = new HashSet<>();

        // This is a very hacky solution to ensure that the PDA never gets
        // stuck in a loop while actually being able to do the epsilon
        // transitions it needs to. It will do as many epsilon transitions
        // as there are states in the machine. Arguably, this is also
        // insufficient, but you can increase the states factor if necessary.
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

    public boolean accepts(String string) {
        // TODO: PDA does not deal with epsilon transitions correctly.
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
