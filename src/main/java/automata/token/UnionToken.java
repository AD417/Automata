package automata.token;

import automata.NFA;
import components.Alphabet;
import operations.AutomataCombiner;

import java.util.List;

public record UnionToken(List<Token> subTokens) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        return subTokens.stream()
                .map(x -> x.convertToNFA(alphabet))
                .reduce(AutomataCombiner::union)
                .orElse(new EmptyToken().convertToNFA(alphabet));
    }

    @Override
    public String toString() {
        return "Union" + subTokens;
    }
}
