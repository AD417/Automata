import automata.AutomataBuilder;
import automata.NFA;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.DFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");
        NFA n = AutomataBuilder.parseExpression("[ab]*ab", alphabet);
        System.out.println(n);
    }
}
