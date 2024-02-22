import automata.AutomataBuilder;
import automata.NFA;
import components.Alphabet;
import components.GeneralTransition;
import components.State;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");
        NFA n = AutomataBuilder.parseExpression(".*ba", alphabet);
        System.out.println(n);

        GeneralTransition gt = new GeneralTransition();
        gt.convertFrom(n.transitionFunction(), n.alphabet());
        System.out.println(gt);
        System.out.println();
        gt.rip(new State("q3"));
        gt.rip(new State("q6"));
        System.out.println(gt);
    }
}
