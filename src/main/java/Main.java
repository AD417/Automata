import automata.AutomataBuilder;
import automata.GNFA;
import automata.NFA;
import components.Alphabet;
import components.GeneralTransition;
import components.State;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");
        NFA n = AutomataBuilder.parseExpression(".*ba", alphabet, false);
        System.out.println(n);

        GNFA g = n.convertToGNFA();
        System.out.println(g);
        System.out.println("REGEX: " + g.toRegex());
    }
}
