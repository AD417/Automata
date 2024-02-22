import automata.AutomataBuilder;
import automata.NFA;
import components.Alphabet;

public class Main {
    public static void main(String[] args) {
        Alphabet alphabet = Alphabet.withSymbols("ab");
        NFA n = AutomataBuilder.parseExpression(".*aba", alphabet);
        System.out.println(n);

        String test = "abbabababbabaaabaabababa";
        n.acceptableSubstrings(test)
                .stream()
                .map(x -> test.substring(0, x))
                .forEach(System.out::println);
    }
}
