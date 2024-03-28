package grammar.components;

public class Symbol implements Element {
    /** A toString representation of this variable. */
    private final char representation;

    public Symbol(char symbol) {
        representation = symbol;
    }

    public String toString() {
        return "" + representation;
    }

    @Override
    public int compareTo(Element o) {
        if (o.getClass() != Symbol.class) {
            return 1;
        }
        return this.toString().compareTo(o.toString());
    }
}
