import automata.PDA;
import automata.components.Alphabet;
import automata.components.StackAlphabet;
import automata.components.StackTransition;
import automata.components.State;
import grammar.CFG;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Grammar g = new Grammar();
        g.addRule('S', "SS | A | B");
        g.addRule('A', "aAb | ab");
        g.addRule('B', "bBa | ba");

        Set<Symbol> symbols = Set.of(new Symbol('a'), new Symbol('b'));
        Set<Variable> variables = Set.of(new Variable("S"), new Variable("A"), new Variable("B"));

        CFG cfg = new CFG(variables, symbols, g, new Variable('S'));

        cfg.sampleStrings(5).forEach(System.out::println);
        System.out.println(cfg);
        System.out.println();

        PDA pushover = cfg.convertToPDA();
        System.out.println(pushover);
        System.out.println(pushover.accepts(""));
        System.out.println(pushover.accepts("abba"));
        System.out.println(pushover.accepts("baba"));
        System.out.println(pushover.accepts("ababbababa"));
        System.out.println(pushover.accepts("aaaaabbbbb"));
        System.out.println(pushover.accepts("bbbbbaaaaa"));
        System.out.println(pushover.accepts("bbbbbaaaaab"));
    }
}
