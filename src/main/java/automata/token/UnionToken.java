package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.operations.AutomataCombiner;

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
        if (subTokens.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(subTokens.get(0));
        for (int i = 1; i < subTokens.size(); i++) {
            sb.append("|").append(subTokens.get(i));
        }
        return sb.toString();
    }
}
