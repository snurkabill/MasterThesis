package vahy.prototype;

import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;
import java.util.SplittableRandom;

public class RandomDistributionSampling {

    public static void main(String[] args) {

        SplittableRandom random = new SplittableRandom(2);
        double tolerance = Math.pow(10, -10);

        double[] distribution = new double[] {0.6, 0.4};
        int[] counter = new int[distribution.length];

        int iterations = 100_000;
        for (int i = 0; i < iterations; i++) {
            counter[RandomDistributionUtils.getRandomIndexFromDistribution(distribution, random)]++;
        }
        double[] sampled = new double[distribution.length];
        for (int i = 0; i < counter.length; i++) {
            sampled[i] = counter[i] / (double)iterations;
        }
        System.out.println(Arrays.toString(sampled));

    }

}
