package grammar;

import grammar.components.CFString;
import grammar.components.Grammar;
import grammar.components.Symbol;
import grammar.components.Variable;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A context-free grammar. Generates strings by applying various rules to create strings.
 * @param variables The set of all the variables that can be made from
 * @param output
 * @param grammar
 * @param start
 */
public record CFG(Set<Variable> variables, Set<Symbol> output, Grammar grammar, Variable start) {
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

    public Stream<String> sampleStrings(int limit) {
        Iterator<String> itr = new CFStringIterator(limit);
        Spliterator<String> sitr = Spliterators.spliteratorUnknownSize(itr, 0);

        return StreamSupport.stream(sitr, false);
    }
}
