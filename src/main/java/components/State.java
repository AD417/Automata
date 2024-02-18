package components;

import java.util.Objects;

/**
 * A state in a Finite Automaton.
 */
public class State implements Comparable<State> {
    /** A counter to create unique states as needed by the user or program. */
    private static int STATE_COUNTER = 0;
    /** The name of this state. Defaults to qXX, where X is a number. */
    private final String name;

    /**
     * Initialize a state with a default name.
     */
    public State() {
        this("q"+STATE_COUNTER++);
    }

    /**
     * Initialize a state with a user-provided name.
     * @param name The name to use. Assumed to be unique.
     */
    public State(String name) {
        this.name = name;
    }

    public static State trap() {
        return new State("TRAP_"+ STATE_COUNTER++);
    }

    /**
     * Get the name of this state.
     * @return The name of this state.
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(name, state.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(State other) {
        return this.name.compareTo(other.name);
    }
}
