package vahy.utils;

import java.util.List;
import java.util.SplittableRandom;

public class RandomDistributionUtils {

    public static double TOLERANCE = Math.pow(10, -10);

    public static boolean isDistribution(List<Double> distribution) {
        double cumulativeSum = 0.0;
        for (double v : distribution) {
            cumulativeSum += v;
            if(v < 0.0 || v > 1.0) {
                return false;
            }
        }
        return Math.abs(1 - cumulativeSum) < TOLERANCE;
    }

//    public static List<Double> normalizeDistribution(List<Double> distribution) {
//        double sum = distribution.stream().mapToD;
//        double[] normalizedDistribution = new double[distribution.length];
//        for (int i = 0; i < distribution.length; i++) {
//            normalizedDistribution[i] = distribution[i] / sum;
//        }
//        return normalizedDistribution;
//    }

    public static int getRandomIndexFromDistribution(List<Double> distribution, SplittableRandom random) {
        if(!isDistribution(distribution)) {
            throw new IllegalArgumentException("Given array does not represent probability distribution");
        }
        double value = random.nextDouble();
        double cumulativeSum = 0.0;
        for (int i = 0; i < distribution.size(); i++) {
            cumulativeSum += distribution.get(i);
            if(value <= cumulativeSum) {
                return i;
            }
        }
        // rounding
        return distribution.size()- 1;
    }

}
