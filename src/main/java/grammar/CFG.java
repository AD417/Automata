package grammar;

import automata.PDA;
import automata.components.Alphabet;
import automata.components.StackAlphabet;
import automata.components.StackTransition;
import automata.components.State;
import grammar.components.CFString;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A context-free grammar. Encodes a set of rules that, when followed from a
 * starting "seed" variable, will produce any string in the relevant
 * Context-Free Language.
 * @param variables The set of all variables used in the CFG parsing.
 * @param alphabet The set of all terminal symbols that can end up in a
 *                 complete CFL string.
 * @param grammar The set of rules that can be used to modify a partially built
 *                CFG string.
 * @param start The initial, or "seed" variable, from which all CFL strings are
 *              derived from by following the provided grammar rules.
 */
public record CFG(Set<Variable> variables,
                  Set<Symbol> alphabet,
                  Grammar grammar,
                  Variable start) {
    private class CFStringIterator implements Iterator<String> {
        final int limit;
        int found = 0;
        PriorityQueue<CFString> toParse = new PriorityQueue<>();
        Set<CFString> seen = new HashSet<>();

        String nextStr;

        public CFStringIterator(int limit) {
            this.limit = limit;
            CFString str = new CFString(start);
            toParse.add(str);
            nextStr = computeNextStr();
        }

        @Override
        public boolean hasNext() {
            return nextStr != null && found < limit;
        }

        @Override
        public String next() {
            String out = nextStr;
            nextStr = computeNextStr();
            found++;
            return out;
        }

        public String computeNextStr() {
            while (!toParse.isEmpty()) {
                CFString cf = toParse.poll();
                assert cf != null;
                if (seen.contains(cf)) continue;
                seen.add(cf);
                if (cf.isComplete()) return cf.toString();

                Set<CFString> out = grammar.applyRule(cf);


                toParse.addAll(out);
            }
            return null;
        }
    }

    /**
     * Provide a stream of strings created by this language. These
     * @param limit The maximum number of strings to create. Will terminate
     *              early if no more strings can be generated.
     * @return A string outputting
     */
    public Stream<String> sampleStrings(int limit) {
        Iterator<String> itr = new CFStringIterator(limit);
        Spliterator<String> sitr = Spliterators.spliteratorUnknownSize(itr, 0);

        return StreamSupport.stream(sitr, false);
    }

    public PDA convertToPDA() {
        Alphabet tapeAlphabet = Alphabet.withSymbols(
                alphabet.stream().map(x -> x.toString().charAt(0)).collect(Collectors.toSet()));

        StackAlphabet stackAlphabet = StackAlphabet.withSymbols(
                variables.stream().map(Variable::toString).collect(Collectors.toSet()));
        stackAlphabet.addAll(tapeAlphabet.stream().map(x -> "" + x).collect(Collectors.toSet()));
        stackAlphabet.add("$");

        State begin = new State("START");
        State loop = new State("LOOP");
        State end = new State("FINAL");
        State extra;
        Set<State> allStates = new HashSet<>(Set.of(begin, loop, end));

        StackTransition st = new StackTransition();

        // Starting transition.
        extra = new State();
        allStates.add(extra);
        st.setState(begin, StackAlphabet.EPSILON, Alphabet.EPSILON, extra, StackAlphabet.CONTROL);
        st.setState(extra, StackAlphabet.EPSILON, Alphabet.EPSILON, loop, start.toString());

        // Looping in-place replacements.
        for (Variable input : grammar().keySet()) {
            for (CFString output : grammar.get(input)) {
                extra = loop;
                State s;
                for (int i = output.size()-1; i >= 0; i--) {
                    if (i == 0) {
                        s = loop;
                    } else {
                        s = new State();
                        allStates.add(s);
                    }
                    // Band aid solution for the first step in a replacement;
                    // Ensures that the transition in pops a symbol.
                    String trans = StackAlphabet.EPSILON;
                    if (i == output.size() - 1) trans = input.toString();
                    st.setState(extra, trans, Alphabet.EPSILON, s, output.get(i).toString());
                    extra = s;
                }
            }
        }
        // Looping popping of stack elements where matches occur.
        for (Character symbol : tapeAlphabet) {
            st.setState(loop, ""+symbol, symbol, loop, StackAlphabet.EPSILON);
        }

        // Ending transition.
        st.setState(loop, StackAlphabet.CONTROL, Alphabet.EPSILON, end, StackAlphabet.EPSILON);

        Set<State> accepting = Set.of(end);

        return new PDA(allStates, tapeAlphabet, stackAlphabet, st, begin, accepting);
    }

    @Override
    public String toString() {
        return "CFG G = (V, Σ, R, S), where:\n" +
                "V = " + variables + "\n" +
                "Σ = " + alphabet + "\n" +
                "R = the following rules:\n" + grammar + "\n" +
                "S = " + start;
    }
}
