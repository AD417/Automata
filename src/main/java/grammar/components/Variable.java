package grammar.components;

import java.util.Objects;

/**
 * An intermediate variable in a CFG generation.
 */
public class Variable implements Element {
    private static int VARIABLE_COUNTER = 0;

    /** A toString representation of this variable. */
    private final String representation;

    public Variable() {
        this("V_" + VARIABLE_COUNTER++);
    }

    public Variable(String symbol) {
        representation = symbol;
    }

    public Variable(char symbol) {
        representation = "" + symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(representation, variable.representation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(representation);
    }

    public String toString() {
        return "[" + representation + "]";
    }

    @Override
    public int compareTo(Element o) {
        if (o.getClass() != Variable.class) {
            return -1;
        }
        return this.toString().compareTo(o.toString());
    }
}
