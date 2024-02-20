import automata.AutomataBuilder;
import automata.NFA;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.DFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Regex to recognize numbers divisible by 3, apparently.
        NFA n = AutomataBuilder.parseExpression("(0*(1(01*0)*1)*)*", Alphabet.withSymbols("01"));
        System.out.println(n);
        DFA d = AutomataConvertor.NFAtoDFA(n).cloneReplaceStates();
        System.out.println(d);

        for (int i = 0; i < 20; i++) {
            System.out.println(i + " :" + d.accepts(Integer.toBinaryString(i)));
        }
    }
}
