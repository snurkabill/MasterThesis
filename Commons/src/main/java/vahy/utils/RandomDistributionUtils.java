package vahy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.SplittableRandom;

public class RandomDistributionUtils {

    private final Logger logger = LoggerFactory.getLogger(RandomDistributionUtils.class.getName());

    public static int SAMPLING_RANOMD_INDEX_TRIAL_COUNT = 10;
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

    public static boolean isDistribution(double[] distribution) {
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

    private static int getRandomIndexFromDistribution(List<Double> distribution, SplittableRandom random, int trialCount) {
        if(trialCount > SAMPLING_RANOMD_INDEX_TRIAL_COUNT) {
            throw new IllegalStateException("Numerically unstable probability calculation");
        }
        double value = random.nextDouble();
        double cumulativeSum = 0.0;
        for (int i = 0; i < distribution.size(); i++) {
            cumulativeSum += distribution.get(i);
            if(value <= cumulativeSum) {
                return i;
            }
        }
        return getRandomIndexFromDistribution(distribution, random, trialCount + 1);
    }

    private static int getRandomIndexFromDistribution(double[] distribution, SplittableRandom random, int trialCount) {
        if(trialCount > SAMPLING_RANOMD_INDEX_TRIAL_COUNT) {
            throw new IllegalStateException("Numerically unstable probability calculation");
        }
        double value = random.nextDouble();
        double cumulativeSum = 0.0;
        for (int i = 0; i < distribution.length; i++) {
            cumulativeSum += distribution[i];
            if(value <= cumulativeSum) {
                return i;
            }
        }
        return getRandomIndexFromDistribution(distribution, random, trialCount + 1);
    }

    public static int getRandomIndexFromDistribution(List<Double> distribution, SplittableRandom random) {
        if(!isDistribution(distribution)) {
            throw new IllegalArgumentException("Given array does not represent probability distribution");
        }
        return getRandomIndexFromDistribution(distribution, random, 0);
    }

    public static int getRandomIndexFromDistribution(double[] distribution, SplittableRandom random) {
        if(!isDistribution(distribution)) {
            throw new IllegalArgumentException("Given array does not represent probability distribution");
        }
        return getRandomIndexFromDistribution(distribution, random, 0);
    }

}
