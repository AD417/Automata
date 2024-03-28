package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.operations.AutomataCombiner;

import java.util.List;

public record ConcatToken(List<Token> subTokens) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        return subTokens.stream()
                .map(x -> x.convertToNFA(alphabet))
                .reduce(AutomataCombiner::concatenate)
                .orElse(new EmptyToken().convertToNFA(alphabet));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : subTokens) sb.append(token);
        return sb.toString();
    }
}
