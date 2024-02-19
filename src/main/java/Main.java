import automata.AutomataBuilder;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.NFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");

        NFA nfa = AutomataBuilder.parseExpression("a[ab][ab]b", alphabet);

        System.out.println(nfa);
        System.out.println();
        System.out.println("N   Accepts 'abab': " + nfa.accepts("abab"));
        System.out.println("N   Accepts 'aaaa': " + nfa.accepts("aaaa"));
        System.out.println("N   Accepts 'bbbb': " + nfa.accepts("bbbb"));
        System.out.println("N   Accepts 'abbb': " + nfa.accepts("abbb"));
        System.out.println(
                "N^2 Accepts 'abbbaaab': " +
                AutomataCombiner.power(nfa,2).accepts("abbbaaab")
        );
    }
}
