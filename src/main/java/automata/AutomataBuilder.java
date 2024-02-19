package automata;

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
        boolean brackets = false;
        int lastPoint = 0;
        NFA result = null;

        for (int i = 0; i < expression.length(); i++) {
            // System.out.println(expression.charAt(i));
            if (brackets) {
                if (expression.charAt(i) == ']') {
                    brackets = false;
                    String symbols = expression.substring(lastPoint, i);
                    lastPoint = i+1;

                    NFA lastSection = forAnySymbol(symbols, alphabet);
                    if (result == null) result = lastSection;
                    else result = AutomataCombiner.concatenate(result, lastSection);
                }
            } else {
                if (expression.charAt(i) == '[') {
                    brackets = true;
                    String literal = expression.substring(lastPoint, i);
                    lastPoint = i+1;
                    if (literal.isEmpty()) continue;

                    NFA lastSection = forLiteral(literal, alphabet);
                    if (result == null) result = lastSection;
                    else result = AutomataCombiner.concatenate(result, lastSection);
                }
            }
        }
        if (brackets) {
            String msg = String.format("Missing close bracket in expression '%s'", expression);
            throw new ParserException(msg);
        }

        String literal = expression.substring(lastPoint);
        if (!literal.isEmpty()) {
            NFA lastSection = forLiteral(literal, alphabet);
            if (result == null) result = lastSection;
            else result = AutomataCombiner.concatenate(result, lastSection);
        }
        assert result != null;
        return result.simplifyEpsilon();
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
