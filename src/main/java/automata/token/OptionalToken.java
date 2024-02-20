package automata.token;

import automata.NFA;
import components.Alphabet;
import operations.AutomataCombiner;

public record OptionalToken(Token subToken) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        NFA optionalNFA = subToken.convertToNFA(alphabet);
        return AutomataCombiner.union(new EmptyToken().convertToNFA(alphabet), optionalNFA);
    }
}
