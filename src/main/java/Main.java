import automata.AutomataBuilder;
import automata.NFA;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.DFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("01");
        NFA n = AutomataBuilder.parseExpression("[01]*001[01]*", alphabet);
        System.out.println(n);
        n = AutomataBuilder.parseExpression("([01][01])*|([01][01][01])*", alphabet);
        System.out.println(n);
        n = AutomataBuilder.parseExpression("1*(011*)*", alphabet);
        System.out.println(n);
        n = AutomataBuilder.parseExpression("|[01]|[01][01]|[01][01][01]", alphabet);
        System.out.println(n);
    }
}
