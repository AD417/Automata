import automata.AutomataBuilder;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.DFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        DFA n = AutomataConvertor.NFAtoDFA(
                AutomataBuilder.parseExpression("ab*a", Alphabet.withSymbols("ab"))
        ).cloneReplaceStates();

        System.out.println(n);
        System.out.println(n.accepts("aa"));
    }
}
