package petrinet;

public class Pair<Y, Z> {
    private Y first;
    private Z second;

    public Pair(Y first, Z second) {
        this.first = first;
        this.second = second;
    }

    public Y getFirst() {
        return first;
    }

    public Z getSecond() {
        return second;
    }
}