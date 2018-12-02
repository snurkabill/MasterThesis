package vahy.utils;

public class ImmutableTriple<T1, T2, T3> {

    private final T1 first;
    private final T2 second;
    private final T3 third;

    public ImmutableTriple(T1 first, T2 second, T3 third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public T3 getThird() {
        return third;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableTriple)) return false;

        ImmutableTriple<?, ?, ?> that = (ImmutableTriple<?, ?, ?>) o;

        if (getFirst() != null ? !getFirst().equals(that.getFirst()) : that.getFirst() != null) return false;
        if (getSecond() != null ? !getSecond().equals(that.getSecond()) : that.getSecond() != null) return false;
        return getThird() != null ? getThird().equals(that.getThird()) : that.getThird() == null;
    }

    @Override
    public int hashCode() {
        int result = getFirst() != null ? getFirst().hashCode() : 0;
        result = 31 * result + (getSecond() != null ? getSecond().hashCode() : 0);
        result = 31 * result + (getThird() != null ? getThird().hashCode() : 0);
        return result;
    }
}
