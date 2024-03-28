package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.operations.AutomataCombiner;

public record OptionalToken(Token subToken) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        NFA optionalNFA = subToken.convertToNFA(alphabet);
        return AutomataCombiner.union(new EmptyToken().convertToNFA(alphabet), optionalNFA);
    }

    @Override
    public String toString() {
        return subToken + "?";
    }
}
