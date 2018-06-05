package vahy.utils;

public class MutableTuple <T1, T2> {

    private T1 first;
    private T2 second;

    public MutableTuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public void setFirst(T1 item) {
        this.first = item;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(T2 item) {
        this.second = item;
    }
}
