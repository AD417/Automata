package components;

import java.util.Objects;

public class State {
    private static int STATE_COUNTER = 0;
    private final String name;

    public State() {
        this("q"+STATE_COUNTER++);
    }
    public State(String name) {
        this.name = name;
    }

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
}
