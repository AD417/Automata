package grammar.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Grammatical rules to be applied to
 */
public class Grammar extends HashMap<Variable, Set<CFString>> {

    /**
     * Add a rule to the Grammar. This rule may be written with multiple outcomes separated by '|' characters.
     * @param input the Variable to input on.
     * @param output the string(s) that the variable may be returned.
     */
    public void addRule(Character input, String output) {
        HashSet<CFString> outcomes = new HashSet<>();
        String rule;
        int start = 0;
        for (int i = 1; i < output.length(); i++) {
            if (output.charAt(i) != '|') continue;
            rule = output.substring(start, i).trim();
            start = i+1;
            outcomes.add(CFString.fromString(rule));
        }
        outcomes.add(CFString.fromString(output.substring(start).trim()));
        this.computeIfAbsent(new Variable(input), k -> new HashSet<>()).addAll(outcomes);
    }

    /**
     * Apply a rule to the first variable in the string.
     * @param str a CFG string to insert symbols on.
     * @return the set of all CFG strings that can be reached by applying
     *         a rule to the input string.
     */
    public Set<CFString> applyRule(CFString str) {
        Set<CFString> out = new HashSet<>();
        IntStream.range(0,str.size()).mapToObj(x -> applyRule(str, x)).forEach(out::addAll);
        return out;
    }

    /**
     * Apply a rule to a specific element in the CFG string, if possible.
     * @param str A CFG string containing a variable to manipulate.
     * @param index The index of the variable to replace based on
     *              this grammar's rules.
     * @return the set of all CFG strings that can be reached by applying a
     *         rule to the input string.
     */
    public Set<CFString> applyRule(CFString str, int index) {
        if (index < 0 || index >= str.size()) return new HashSet<>();

        Element e = str.get(index);
        if (e.getClass() != Variable.class) return new HashSet<>();
        Variable var = (Variable) e;
        if (!containsKey(var)) return new HashSet<>();

        return get(var).stream()
                .map(x -> str.cloneReplace(index, x))
                .collect(Collectors.toSet());
    }
}
