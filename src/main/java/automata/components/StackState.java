package automata.components;

public record StackState(State state, String stackSymbol) implements Comparable<StackState> {

    @Override
    public int compareTo(StackState o) {
        int out = this.state.compareTo(o.state);
        if (out == 0) out = this.stackSymbol.compareTo(o.stackSymbol);
        return out;
    }

    @Override
    public String toString() {
        return state + ", " + stackSymbol;
    }
}
