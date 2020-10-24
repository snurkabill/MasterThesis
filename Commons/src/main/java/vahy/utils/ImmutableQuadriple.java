package vahy.utils;

public class ImmutableQuadriple<T1, T2, T3, T4> {

    private final T1 first;
    private final T2 second;
    private final T3 third;
    private final T4 fourth;

    public ImmutableQuadriple(T1 first, T2 second, T3 third, T4 fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
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

    public T4 getFourth() {
        return fourth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableQuadriple)) return false;

        ImmutableQuadriple<?, ?, ?, ?> that = (ImmutableQuadriple<?, ?, ?, ?>) o;

        if (getFirst() != null ? !getFirst().equals(that.getFirst()) : that.getFirst() != null) return false;
        if (getSecond() != null ? !getSecond().equals(that.getSecond()) : that.getSecond() != null) return false;
        if (getThird() != null ? !getThird().equals(that.getThird()) : that.getThird() != null) return false;
        return getFourth() != null ? getFourth().equals(that.getFourth()) : that.getFourth() == null;
    }

    @Override
    public int hashCode() {
        int result = getFirst() != null ? getFirst().hashCode() : 0;
        result = 31 * result + (getSecond() != null ? getSecond().hashCode() : 0);
        result = 31 * result + (getThird() != null ? getThird().hashCode() : 0);
        result = 31 * result + (getFourth() != null ? getFourth().hashCode() : 0);
        return result;
    }
}
