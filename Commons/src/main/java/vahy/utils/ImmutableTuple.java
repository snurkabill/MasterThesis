package vahy.utils;

public class ImmutableTuple<T1, T2> {

    private final T1 first;
    private final T2 second;

    public ImmutableTuple(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableTuple)) {
            return false;
        }
        ImmutableTuple<?, ?> that = (ImmutableTuple<?, ?>) o;
        if (getFirst() != null ? !getFirst().equals(that.getFirst()) : that.getFirst() != null) {
            return false;
        }
        return getSecond() != null ? getSecond().equals(that.getSecond()) : that.getSecond() == null;
    }

    @Override
    public int hashCode() {
        int result = getFirst() != null ? getFirst().hashCode() : 0;
        result = 31 * result + (getSecond() != null ? getSecond().hashCode() : 0);
        return result;
    }
}
