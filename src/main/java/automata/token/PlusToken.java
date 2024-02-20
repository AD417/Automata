package automata.token;

import automata.NFA;
import components.Alphabet;
import operations.AutomataCombiner;
import operations.AutomataConvertor;

public record PlusToken(Token subToken) implements Token {
    @Override
    public NFA convertToNFA(Alphabet alphabet) {
        NFA singleIteration = subToken.convertToNFA(alphabet);
        return AutomataCombiner.concatenate(
                singleIteration.cloneReplaceStates(),
                AutomataCombiner.kleeneStar(singleIteration)
        );
    }
}
