package automata.components;

public record StatePair(State first, State second) {
    public StatePair(State first, State second) {
        if (first.compareTo(second) > 0) {
            this.first = first;
            this.second = second;
        } else {
            this.second = first;
            this.first = second;
        }
    }
}
