package automata.token;

import automata.NFA;
import automata.components.Alphabet;
import automata.operations.AutomataCombiner;

public record PlusToken(Token subToken) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        NFA singleIteration = subToken.convertToNFA(alphabet);
        return AutomataCombiner.concatenate(
                singleIteration.cloneReplaceStates(),
                AutomataCombiner.kleeneStar(singleIteration)
        );
    }

    @Override
    public String toString() {
        return subToken + "+";
    }
}
