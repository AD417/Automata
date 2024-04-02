package automata;

import automata.components.Alphabet;
import automata.components.StackState;
import automata.components.StackTransition;
import automata.components.State;
import automata.exception.AlphabetException;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
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
                  Set<Character> stackAlphabet,
                  StackTransition relation,
                  State startState,
                  Set<State> acceptingStates) {

    private record PDAConfiguration(State state, Stack<Character> stack) {}

    /**
     * The starting configuration, including the stack.
     * @return a configuration including the start state and empty stack.
     */
    private PDAConfiguration startConfig() {
        return new PDAConfiguration(startState, new Stack<>());
    }

    private Stack<Character> nextStack(Stack<Character> current, Character symbol, boolean isEpsilon) {
        @SuppressWarnings("unchecked")
        Stack<Character> next = (Stack<Character>) current.clone();
        if (!isEpsilon) next.pop();
        if (symbol != Alphabet.EPSILON) {
            next.push(symbol);
        }
        return next;
    }

    private Set<PDAConfiguration> transitionStep(PDAConfiguration config, Character symbol) {
        Set<PDAConfiguration> normalStep = new HashSet<>();
        if (symbol == Alphabet.EPSILON) normalStep.add(config);

        for (StackState eResult : relation.epsilonStackTransition(config.state, symbol)) {
            //System.out.println(config.state + ", ε, " + symbol + " --> " + eResult);
            Stack<Character> futureStack = nextStack(config.stack, eResult.stackSymbol(), true);
            normalStep.add(new PDAConfiguration(eResult.state(), futureStack));
        }
        if (!config.stack.isEmpty()) {
            StackState input = new StackState(config.state, config.stack.peek());
            for (StackState result : relation.transition(input, symbol)) {
                //System.out.println(input + ", " + symbol + " --> " + result);
                Stack<Character> futureStack = nextStack(config.stack, result.stackSymbol(), false);
                normalStep.add(new PDAConfiguration(result.state(), futureStack));
            }
        }

        Set<PDAConfiguration> epsilonStep = new HashSet<>(normalStep);
        if (symbol == Alphabet.EPSILON) epsilonStep.add(config);

        for (PDAConfiguration pda : normalStep) {
            for (StackState result : relation.epsilonStackTransition(config.state, symbol)) {
                Stack<Character> futureStack = nextStack(config.stack, result.stackSymbol(), true);
                epsilonStep.add(new PDAConfiguration(result.state(), futureStack));
            }
            if (!pda.stack.isEmpty()) {
                StackState input = new StackState(pda.state, pda.stack.peek());
                for (StackState result : relation.transition(input, symbol)) {
                    Stack<Character> futureStack = nextStack(pda.stack, result.stackSymbol(), false);
                    epsilonStep.add(new PDAConfiguration(result.state(), futureStack));
                }
            }
        }
        return epsilonStep;
    }

    public boolean accepts(String string) {
        Set<PDAConfiguration> currentConfigs = Set.of(startConfig());
        currentConfigs = currentConfigs.stream()
                .map(c -> transitionStep(c, Alphabet.EPSILON))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

        currentConfigs.forEach(System.out::println);

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
                    .map(c -> transitionStep(c, Alphabet.EPSILON))
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            currentConfigs.forEach(System.out::println);
            System.out.println();
        }
        return currentConfigs.stream().anyMatch(x -> acceptingStates.contains(x.state));
    }
}
