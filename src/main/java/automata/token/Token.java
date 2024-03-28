package automata.token;

import automata.NFA;
import automata.components.Alphabet;

public interface Token {
    NFA convertToNFA(Alphabet alphabet);
}
