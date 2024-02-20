package automata.token;

import automata.NFA;
import components.Alphabet;
import operations.AutomataCombiner;

public record KleeneToken(Token baseToken) implements Token {

    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        return AutomataCombiner.kleeneStar(baseToken.convertToNFA(alphabet));
    }

    @Override
    public String toString() {
        return "Star[" + baseToken + "]";
    }
}
