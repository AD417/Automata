import grammar.CFG;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        int SIZE = 4;
        Grammar g = new Grammar();
        g.addRule('S', "O | E");
        g.addRule('O', "0O0 | 0O1 | 1O0 | 1O1 | 0 | 1");
        g.addRule('E', "AB | BA");
        g.addRule('A', "0A0 | 0A1 | 1A0 | 1A1 | 0");
        g.addRule('B', "0B0 | 0B1 | 1B0 | 1B1 | 1");

        Set<Symbol> symbols = Set.of(new Symbol('0'), new Symbol('1'));
        Set<Variable> variables = Set.of(new Variable("S"),
                                         new Variable("E"),
                                         new Variable("O"),
                                         new Variable("A"),
                                         new Variable("B"));

        CFG cfg = new CFG(variables, symbols, g, new Variable('S'));

        Set<String> language = cfg.sampleStrings(10000).filter(x -> x.length() == SIZE*2).collect(Collectors.toSet());

        int errors = 0;
        for (int i = 0; i < 1 << (SIZE*2); i++) {
            String s = "00".repeat(SIZE) + Integer.toBinaryString(i);
            s = s.substring(s.length()-(SIZE*2));
            if (s.substring(0, SIZE).equals(s.substring(SIZE, SIZE*2))) continue;
            if (language.contains(s)) continue;
            System.out.println(s);
            errors++;
        }
        System.out.println("> RESULT: " + errors + " Missing values detected.");
        System.out.println(cfg);
    }
}
