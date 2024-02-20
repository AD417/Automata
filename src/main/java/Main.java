import automata.AutomataBuilder;
import operations.AutomataCombiner;
import operations.AutomataConvertor;
import automata.NFA;
import components.*;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println(AutomataBuilder.recursiveParse("a[ab]*aaab"));
    }
}
