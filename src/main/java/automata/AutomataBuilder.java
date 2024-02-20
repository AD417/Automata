package automata;

import automata.token.*;
import components.Alphabet;
import components.State;
import components.Transition;
import exception.AlphabetException;
import exception.ParserException;
import operations.AutomataCombiner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class AutomataBuilder {
    // TODO: literally should be a class.
    public static NFA parseExpression(String expression, Alphabet alphabet) {
        return recursiveParse(expression).stream()
                .map(x -> x.convertToNFA(alphabet))
                .reduce(AutomataCombiner::concatenate)
                .orElse(new EmptyToken().convertToNFA(alphabet))
                .simplifyEpsilon();
    }

    public static List<Token> recursiveParse(String expression) {
        List<Token> tokens = new LinkedList<>();
        for (int i = 0; i < expression.length(); i++) {
            switch (expression.charAt(i)) {
                case '*' -> {
                    Token token = tokens.remove(tokens.size() - 1);
                    tokens.add(new KleeneToken(token));
                }
                case '[' -> {
                    int first = ++i;
                    while (expression.charAt(i) != ']') i++;
                    tokens.add(new ChoiceToken(expression.substring(first, i)));
                }
                case '(' -> {
                    int first = ++i;
                    while (expression.charAt(i) != ')') i++;
                    List<Token> groupTokens = recursiveParse(expression.substring(first, i));
                    tokens.add(new GroupToken(groupTokens));
                }
                default -> tokens.add(new CharacterToken(expression.charAt(i)));
            }
        }
        return tokens;
    }

    /**
     * Construct an NFA that only accepts the empty string.
     * @param alphabet the Alphabet under consideration.
     * @return a NFA that matches only the empty set.
     */
    public static NFA forEmpty(Alphabet alphabet) {
        return forLiteral("", alphabet);
    }

    /**
     * Construct an NFA that matches exactly the provided string.
     * @param string the String to match.
     * @param alphabet the Alphabet under consideration.
     * @return a NFA that matches only the string provided, and nothing else.
     */
    public static NFA forLiteral(String string, Alphabet alphabet) {
        List<State> states = new LinkedList<>();
        // First state.
        states.add(new State());
        for (Character __ : string.toCharArray()) states.add(new State());

        Set<State> stateSet = new HashSet<>(states);
        Transition tf = new Transition();
        // TODO: allow for Transition function initialization using any collection type.
        //  Might be pointless?
        tf.initializeFor(stateSet, alphabet);

        for (int i = 0; i < string.length(); i++) {
            Character symbol = string.charAt(i);
            if (!alphabet.contains(symbol)) {
                String msg = String.format(
                        "String '%s' contains symbol '%c' not in alphabet.",
                        string, symbol
                );
                throw new AlphabetException(msg);
            }
            tf.setState(states.get(i), symbol, states.get(i+1));
        }

        return new NFA(stateSet, alphabet, tf, states.get(0), Set.of(states.get(states.size()-1)));
    }

    /**
     * Construct an NFA that will accept exactly one and exactly one of
     * the provided symbols.
     * @param symbols the list of symbols that may be accepted.
     * @param alphabet the Alphabet under consideration.
     * @return an NFA that will match any of the provided symbols one time.
     */
    public static NFA forAnySymbol(String symbols, Alphabet alphabet) {
        State initial = new State();
        State accepting = new State();

        Set<State> states = Set.of(initial, accepting);
        Transition tf = new Transition();
        tf.initializeFor(states, alphabet);
        for (Character symbol : symbols.toCharArray()) {
            if (!alphabet.contains(symbol)) {
                String msg = String.format(
                        "Symbol list [%s] contains symbol '%c' not in alphabet.",
                        symbols, symbol
                );
                throw new AlphabetException(msg);
            }
            tf.setState(initial, symbol, accepting);
        }
        return new NFA(states, alphabet, tf, initial, Set.of(accepting));
    }
}
