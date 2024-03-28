package grammar.components;

import java.util.ArrayList;

public class CFString extends ArrayList<Element> implements Comparable<CFString> {
    private CFString(int size) {
        super(size);
    }

    public CFString(Element seed) {
        super();
        add(seed);
    }

    /**
     * Convert a string of alphabet symbols to a Context-Free String. The
     * conversion assumes uppercase characters are variables and lowercase
     * variables are output symbols.
     */
    public static CFString fromString(String string) {
        CFString str = new CFString(string.length());
        for (char c : string.toCharArray()) {
            Element e;
            if ('A' <= c && c <= 'Z') {
                e = new Variable(c);
            } else {
                e = new Symbol(c);
            }
            str.add(e);
        }
        return str;
    }

    /**
     * Determine the index of the first variable in this string, if applicable.
     * @return the index of the first variable, or -1 if none exist.
     */
    public int firstVariable() {
        for (int i = 0; i < size(); i++) {
            if (get(i).getClass() == Variable.class) return i;
        }
        return -1;
    }

    /**
     * Determine if a string is complete -- if it cannot be edited further by
     * variables.
     * @return true iff the string does not contain any more variables.
     */
    public boolean isComplete() {
        return stream().allMatch(x -> x.getClass() == Symbol.class);
    }

    public CFString cloneReplace(int index, CFString rule) {
        CFString out = new CFString(this.size() - 1 + rule.size());
        out.addAll(subList(0, index));
        out.addAll(rule);
        out.addAll(subList(index + 1, size()));

        return out;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Element e : this) sb.append(e);
        return sb.toString();
    }

    @Override
    public int compareTo(CFString o) {
        int out = this.size() - o.size();
        if (out != 0) return out;

        for (int i = 0; i < this.size(); i++) {
            out = get(i).compareTo(o.get(i));
            if (out != 0) break;
        }
        return out;
    }
}
