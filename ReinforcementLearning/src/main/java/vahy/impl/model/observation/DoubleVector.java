package vahy.impl.model.observation;

import vahy.api.model.observation.Observation;

import java.util.Arrays;

public class DoubleVector implements Observation {

    private static final double[] primeNumbers = {
        127, 271, 331, 397, 547,
        631, 919, 1657, 1801, 1951,
        2269, 2437, 2791, 3169, 3571,
        4219, 4447, 5167, 5419, 6211,
        7057, 7351, 8269, 9241, 10267,
        11719, 12097, 13267, 13669, 16651,
        19441, 19927, 22447, 23497, 24571,
        25117, 26227, 27361, 33391, 35317};


    private static final double[] PRIME_NUMBERS = {
        30402457, 32582657, 37156667, 42643801, 43112609,
        6972593, 13466917, 20996011, 24036583, 25964951,
        859433, 1257787, 1398269, 2976221, 3021377,
        86243, 110503, 132049, 216091, 756839,
        11213, 19937, 21701, 23209, 44497,
        3217, 4253, 4423, 9689, 9941,
        521, 607, 1279, 2203, 2281,
        666649, 946669, 60000049, 66000049, 66600049,
        10619863, 6620830889d, 80630964769d, 228204732751d, 1171432692373d,
        1398341745571d, 10963707205259d, 15285151248481d, 10657331232548839d, 790738119649411319d,
        18987964267331664557d,
    };

    private final double[] observedVector;
    private int hash;
    private boolean isHashCalculated;

    public DoubleVector(double[] observedVector) {
        this.observedVector = observedVector;
        this.isHashCalculated = false;
    }

    public double[] getObservedVector() {
        return observedVector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoubleVector)) return false;

        DoubleVector that = (DoubleVector) o;

        return Arrays.equals(getObservedVector(), that.getObservedVector());
    }

//    @Override
//    public int hashCode() {
//        return Arrays.hashCode(getObservedVector());
//    }


    @Override
    public int hashCode() {
        if(!isHashCalculated) {
            var doubleArrayForHash = new double[observedVector.length];

            for (int i = 0; i < doubleArrayForHash.length; i++) {
                doubleArrayForHash[i] = observedVector[i] * PRIME_NUMBERS[i % PRIME_NUMBERS.length];
            }
            this.hash = Arrays.hashCode(doubleArrayForHash);
            this.isHashCalculated = true;
        }
        return hash;
    }

}
