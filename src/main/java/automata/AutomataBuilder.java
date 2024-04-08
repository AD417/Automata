package automata;

import automata.token.*;
import automata.components.Alphabet;
import automata.components.State;
import automata.components.Transition;
import automata.exception.AlphabetException;
import automata.operations.AutomataCombiner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * NFA Constructor that converts a (simplified) Regular Expression string
 * into an NFA. <br>
 * Automaton construction involves 5 basic operations:
 * <ol>
 *     <li>An empty string</li>
 *     <li>A single symbol</li>
 *     <li>The union of several substrings</li>
 *     <li>The concatenation of two substrings</li>
 *     <li>A kleene star operation applied to a substring</li>
 * </ol>
 * Any Regex String can be deconstructed into these basic operations. <br>

 */
public class AutomataBuilder {
    /**
     * Compile a regex expression into a NFA. Assumes the Regex string is
     * valid.
     * @param expression The Regular Expression operation to convert into an
     *                   NFA.
     * @param alphabet The Alphabet that the NFA will use. Must contain all the
     *                 symbols explicitly used in the expression.
     * @return a NFA that will match any string that matches the input regex.
     */
    public static NFA parseExpression(String expression, Alphabet alphabet) {
        return parseExpression(expression, alphabet, true);
    }

    /**
     * Compile a regex expression into a NFA. Assumes the Regex string is
     * valid.
     * @param expression The Regular Expression operation to convert into an
     *                   NFA.
     * @param alphabet The Alphabet that the NFA will use. Must contain all the
     *                 symbols explicitly used in the expression.
     * @param simplify Whether the final NFA should be simplified (have all
     *                 epsilon transitions removed).
     * @return a NFA that will match any string that matches the input regex.
     */
    public static NFA parseExpression(String expression, Alphabet alphabet, boolean simplify) {
        NFA result = recursiveParse(expression).stream()
                .map(x -> x.convertToNFA(alphabet))
                .reduce(AutomataCombiner::concatenate)
                .orElse(new EmptyToken().convertToNFA(alphabet));

        if (simplify) return  result.simplifyEpsilon();
        return result;
    }

    /**
     * Recursively parse an input expression and convert it into a List of
     * tokens.
     * Each token represents one of the 5 main rules, or a modification /
     * simplification of an existing rule.
     * Recursion is required in the event that a union of several
     * sub-expressions is required. Each sub-string is tokenized, and then the
     * union of all those tokens is created as a single token.
     * @param expression The part of an expression to parse.
     * @return A list of tokens based on the provided (sub)string.
     */
    private static List<Token> recursiveParse(String expression) {
        List<Token> tokens = new LinkedList<>();
        List<Token> unions = new LinkedList<>();
        for (int i = 0; i < expression.length(); i++) {
            switch (expression.charAt(i)) {
                case '.' -> tokens.add(new AlphabetToken());
                case '*' -> {
                    Token token = tokens.remove(tokens.size() - 1);
                    tokens.add(new KleeneToken(token));
                }
                case '+' -> {
                    Token token = tokens.remove(tokens.size() - 1);
                    tokens.add(new PlusToken(token));
                }
                case '?' -> {
                    Token token = tokens.remove(tokens.size() - 1);
                    tokens.add(new OptionalToken(token));
                }
                case '[' -> {
                    int first = ++i;
                    while (expression.charAt(i) != ']') i++;
                    tokens.add(new ChoiceToken(expression.substring(first, i)));
                }
                case '(' -> {
                    int first = ++i;
                    for (int layers = 1; layers > 0; i++) {
                        if (expression.charAt(i) == '(') layers++;
                        if (expression.charAt(i) == ')') layers--;
                    }
                    i--;
                    List<Token> groupTokens = recursiveParse(expression.substring(first, i));
                    tokens.add(new GroupToken(groupTokens));
                }
                case '|' -> {
                    unions.add(new GroupToken(tokens));
                    tokens = new LinkedList<>();
                }
                default -> tokens.add(new CharacterToken(expression.charAt(i)));
            }
        }
        if (unions.isEmpty()) return tokens;

        unions.add(new GroupToken(tokens));
        return List.of(new UnionToken(unions));
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
