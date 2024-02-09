package components;

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
}
