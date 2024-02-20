package automata.token;

import automata.NFA;
import components.Alphabet;

public interface Token {
    NFA convertToNFA(Alphabet alphabet);
}
