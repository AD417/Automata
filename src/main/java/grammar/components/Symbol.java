package grammar.components;

/**
 * A terminal symbol in a CFG string.
 */
public class Symbol implements Element {
    /** A toString representation of this variable. */
    private final char symbol;

    /**
     * Create a terminal symbol that converts to the provided character.
     * @param symbol the terminal character this represents
     */
    public Symbol(char symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return "" + symbol;
    }

    @Override
    public int compareTo(Element o) {
        if (o.getClass() != Symbol.class) {
            return 1;
        }
        return this.toString().compareTo(o.toString());
    }
}
