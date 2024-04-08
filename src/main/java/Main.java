import automata.PDA;
import grammar.CFG;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Grammar g = new Grammar();
        g.addRule('S', "SS | A | B");
        g.addRule('A', "aAb | ab");
        g.addRule('B', "bBa | ba");

        Set<Symbol> symbols = Set.of(new Symbol('a'), new Symbol('b'));
        Set<Variable> variables = Set.of(new Variable("S"), new Variable("A"), new Variable("B"));

        CFG cfg = new CFG(variables, symbols, g, new Variable('S'));

        /*List<String> strs = cfg.sampleStrings(78).toList();
        for (int i = 0; i < 78; i++) {
            System.out.println(i + ": " + strs.get(i));
        }*/
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
