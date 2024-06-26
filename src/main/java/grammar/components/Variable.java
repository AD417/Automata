package grammar.components;

import java.util.Objects;

/**
 * An intermediate variable in a CFG generation.
 */
public class Variable implements Element {
    /** Internal counter to ensure unique variables. */
    private static int ID_COUNTER = 0;

    /** A toString name for this variable. */
    private final String name;

    /**
     * Create a variable with an autogenerated variable name.
     */
    public Variable() {
        this("V_" + ID_COUNTER++);
    }

    /**
     * Create a variable with the provided name.
     * @param name the name of this Variable.
     */
    public Variable(String name) {
        this.name = name;
    }

    public Variable(char symbol) {
        name = "" + symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(name, variable.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return "[" + name + "]";
    }

    @Override
    public int compareTo(Element o) {
        if (o.getClass() != Variable.class) return -1;
        return this.toString().compareTo(o.toString());
    }
}
