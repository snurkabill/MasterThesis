package vahy.utils;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class RandomDistributionUtils {

    private static final Logger logger = LoggerFactory.getLogger(RandomDistributionUtils.class.getName());

    public static int SAMPLING_RANDOM_INDEX_TRIAL_COUNT = 10;
    public static double TOLERANCE = Math.pow(10, -5);

    public static <T extends Enum<T>> boolean isDistribution(EnumMap<T, Double> map, double tolerance) {
        double cumulativeSum = 0.0;
        for (Double v : map.values()) {
            cumulativeSum += v;
            if(v < 0.0 - tolerance || v > 1.0 + tolerance) {
                return false;
            }
        }
        return Math.abs(1 - cumulativeSum) < tolerance;
    }

    public static boolean isDistribution(List<Double> distribution, double tolerance) {
        double cumulativeSum = 0.0;
        for (double v : distribution) {
            cumulativeSum += v;
            if(v < 0.0 - tolerance || v > 1.0 + tolerance) {
                return false;
            }
        }
        return Math.abs(1 - cumulativeSum) < tolerance;
    }

    public static boolean isDistribution(double[] distribution, double tolerance) {
        double cumulativeSum = 0.0;
        for (double v : distribution) {
            cumulativeSum += v;
            if(v < 0.0 - tolerance || v > 1.0 + tolerance) {
                return false;
            }
        }
        return Math.abs(1 - cumulativeSum) < tolerance;
    }

//    public static List<Double> normalizeDistribution(List<Double> distribution) {
//        double sum = distribution.stream().mapToD;
//        double[] normalizedDistribution = new double[distribution.length];
//        for (int i = 0; i < distribution.length; i++) {
//            normalizedDistribution[i] = distribution[i] / sum;
//        }
//        return normalizedDistribution;
//    }

    public static <T extends Enum<T>> T getRandomElementFromMapDistribution(EnumMap<T, Double> map, SplittableRandom random) {
        if(!isDistribution(map, Math.pow(10, -5))) {
            throw new IllegalArgumentException("Given map does not represent probability distribution over keys. Map: [" + map.toString() + "]");
        }

        for (int trialNumber = 0; trialNumber <= SAMPLING_RANDOM_INDEX_TRIAL_COUNT; trialNumber++) {
            double value = random.nextDouble();
            double cumulativeSum = 0.0;
            for (Map.Entry<T, Double> tDoubleEntry : map.entrySet()) {
                cumulativeSum += tDoubleEntry.getValue();
                if(value <= cumulativeSum) {
                    return tDoubleEntry.getKey();
                }
            }
        }
        throw new IllegalStateException("Numerically unstable probability calculation from distribution: [" + map.toString() + "]");
    }

    public static int getRandomIndexFromDistribution(List<Double> distribution, SplittableRandom random) {
        if(!isDistribution(distribution, Math.pow(10, -5))) {
            throw new IllegalArgumentException("Given array does not represent probability distribution. Array: [" + distribution + "]");
        }
        for (int trialNumber = 0; trialNumber <= SAMPLING_RANDOM_INDEX_TRIAL_COUNT; trialNumber++) {
            double value = random.nextDouble();
            double cumulativeSum = 0.0;
            for (int i = 0; i < distribution.size(); i++) {
                cumulativeSum += distribution.get(i);
                if(value <= cumulativeSum) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("Numerically unstable probability calculation from distribution: [" + distribution
            .stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", ")) + "]");
    }

    public static int getRandomIndexFromDistribution(double[] distribution, SplittableRandom random) {
        if(!isDistribution(distribution, TOLERANCE)) {
            throw new IllegalArgumentException("Given array does not represent probability distribution Array: [" + Arrays.toString(distribution) + "]");
        }
        for (int trialNumber = 0; trialNumber <= SAMPLING_RANDOM_INDEX_TRIAL_COUNT; trialNumber++) {
            double value = random.nextDouble();
            double cumulativeSum = 0.0;
            for (int i = 0; i < distribution.length; i++) {
                cumulativeSum += distribution[i];
                if (value <= cumulativeSum) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("Numerically unstable probability calculation from distribution: [" + Arrays.toString(distribution) +"]");
    }

    public static void applyMaskToRandomDistribution(double[] distribution, boolean[] mask) {
        if(mask.length != distribution.length) {
            throw new IllegalArgumentException("Mask has different length [" + mask.length + "] from distribution length: [" + distribution.length + "]");
        }
        double remainingSum = 0.0;
        int remainingElementsCount = 0;
        for (int i = 0; i < distribution.length; i++) {
            if(mask[i]) {
                remainingSum += distribution[i];
                remainingElementsCount++;
            } else {
                distribution[i] = 0.0;
            }
        }
        if(remainingElementsCount == 0) {
            throw new IllegalStateException("Undefined state: masked whole distribution.");
        } else if(remainingSum == 0.0) {
            var fraction = 1.0 / remainingElementsCount;
            for (int i = 0; i < distribution.length; i++) {
                if(mask[i]) {
                    distribution[i] = fraction;
                }
            }
        } else {
            var ratio = 1.0 / remainingSum;
            for (int i = 0; i < distribution.length; i++) {
                if(mask[i]) {
                    distribution[i] *= ratio;
                }
            }
        }
    }

    public static void applyBoltzmannNoise(double[] distribution, double temperature) {
        applyTemperatureNoise(distribution, temperature);
        applySoftmax(distribution);
    }

    public static void applyTemperatureNoise(double[] distribution, double temperature) {
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = distribution[i] / temperature;
        }
    }

    public static void applySoftmax(double[] distribution) {
        double max = Double.MIN_VALUE;
        for (double entry : distribution) {
            if (entry > max) {
                max = entry;
            }
        }
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = Math.exp(distribution[i] - max);
        }
        double sum = 0.0;
        for (double entry : distribution) {
            sum += entry;
        }
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = distribution[i] / sum;
        }
    }

    public static void tryToRoundDistribution(double[] distribution, double tolerance) {
        for (int i = 0; i < distribution.length; i++) {
            if(distribution[i] <= 0.0 && distribution[i] + tolerance >= 0.0) {
                distribution[i] = 0;
            } else if(distribution[i] >= 1.0 && distribution[i] <= 1.0 + tolerance) {
                distribution[i] = 1.0;
            }
        }
    }

    public static void hardRoundDistribution(double[] distribution) {
        for (int i = 0; i < distribution.length; i++) {
            if(distribution[i] <= 0.0 && distribution[i] + TOLERANCE >= 0.0) {
                distribution[i] = 0;
            } else if(distribution[i] >= 1.0 && distribution[i] <= 1.0 + TOLERANCE) {
                distribution[i] = 1.0;
            }
        }
    }

    public static ImmutableTuple<Boolean, double[]> findProbabilityDistributionUsingLagrange(double[] rewardArray, double[] riskArray, double totalRisk) {
        Set<Integer> negativeIndexes = new HashSet<>();
        int iterations = 0;
        while (iterations <= rewardArray.length) {
            RandomDistributionUtils.logger.debug("Running [{}]th iteration of solution search for new distribution", iterations);
            var hasNegativeElements = false;
            var newDistribution = findSimilarSuitableDistributionByLeastSquares(rewardArray, riskArray, totalRisk, negativeIndexes);
            for (int i = 0; i < rewardArray.length; i++) {
                if(newDistribution[i] < 0.0) {
                    hasNegativeElements = true;
                    negativeIndexes.add(i);
                }
            }
            if(!hasNegativeElements) {
                return new ImmutableTuple<>(true, newDistribution);
            } else {
                iterations++;
            }
        }
        logger.warn("System of linear equations for reward array [" + Arrays.toString(rewardArray) +
            "] and risk array: [" + Arrays.toString(riskArray) + "] with totalRisk: [" + totalRisk + "] has no solution");
        return new ImmutableTuple<>(false, new double[0]);
    }

    private static double[] findProbabilityDistributionUsingLagrange(double[] rewardArray, double[] riskArray, double totalRisk, Set<Integer> negativeIndexes) {

        RandomDistributionUtils.logger.debug("negative indexes: [{}]", negativeIndexes.toString());

        int distributionSize = rewardArray.length;
        int helpVariableCount = 2;
        int negativeHelpVariableCount = negativeIndexes.size();
        int automaticColumnCount = distributionSize + negativeHelpVariableCount;
        int totalColumnCount = helpVariableCount + automaticColumnCount;


        double[][] lhsArray = new double[totalColumnCount][totalColumnCount];
        double[] rhsArray = new double[totalColumnCount];

        // A
        for (int i = 0; i < distributionSize; i++) {
            for (int j = 0; j < distributionSize; j++) {
                lhsArray[i][j] = i == j ? 1 : 0;
            }
            for (int j = 0; j < negativeHelpVariableCount; j++) {
                lhsArray[i][j + distributionSize] = negativeIndexes.contains(i) ? -1 : 0;
            }
            lhsArray[i][automaticColumnCount] = 1;
            lhsArray[i][automaticColumnCount + 1] = riskArray[i];
            rhsArray[i] = -rewardArray[i];
        }

        // D
        int negativeIndexesAdded = 0;
        for (Integer negativeIndex : negativeIndexes) {
            for (int j = 0; j < distributionSize; j++) {
                lhsArray[distributionSize + negativeIndexesAdded][negativeIndex] = 1;
            }
            rhsArray[distributionSize + negativeIndexesAdded] = 0;
            negativeIndexesAdded++;
        }


        // B
        for (int i = 0; i < distributionSize; i++) {
            lhsArray[automaticColumnCount][i] = 1;
        }
        rhsArray[automaticColumnCount] = 1;


        // C
        for (int i = 0; i < distributionSize; i++) {
            lhsArray[automaticColumnCount + 1][i] = riskArray[i];
        }
        rhsArray[automaticColumnCount + 1] = totalRisk;

        try {
            RealMatrix realMatrix = new BlockRealMatrix(lhsArray);
            SingularValueDecomposition singularValueDecomposition = new SingularValueDecomposition(realMatrix);
//            LUDecomposition luDecomposition = new LUDecomposition(realMatrix);
//            DecompositionSolver solver = luDecomposition.getSolver();
            DecompositionSolver solver = singularValueDecomposition.getSolver();
            RealVector resultVector = solver.solve(new ArrayRealVector(rhsArray));

            var result = resultVector.toArray();

            var newDistribution = new double[distributionSize];
            for (int i = 0; i < distributionSize; i++) {
                newDistribution[i] = result[i];
            }
            hardRoundDistribution(newDistribution);
            return newDistribution;
        } catch(Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error occurred during solving linear equations");
            sb.append(System.lineSeparator());
            sb.append("Reward array: ");
            sb.append(Arrays.toString(rewardArray));
            sb.append(System.lineSeparator());
            sb.append("Risk reachability: ");
            sb.append(Arrays.toString(riskArray));
            sb.append(System.lineSeparator());
            sb.append("Total risk allowed: ");
            sb.append(totalRisk);
            sb.append(System.lineSeparator());
            sb.append("Indexes of negative variables: ");
            sb.append(negativeIndexes.toString());
            sb.append(System.lineSeparator());
            sb.append("Right side of equations: ");
            sb.append(Arrays.toString(rhsArray));
            sb.append(System.lineSeparator());
            sb.append("Left side of equations: ");
            sb.append(Arrays.deepToString(lhsArray));
            sb.append(System.lineSeparator());
            throw new IllegalStateException(sb.toString(), e);
        }
    }


    public static ImmutableTuple<Boolean, double[]> findSimilarSuitableDistributionByLeastSquares(double[] distribution, double[] riskArray, double totalRisk) {
        Set<Integer> negativeIndexes = new HashSet<>();
        int iterations = 0;
        while (iterations <= distribution.length) {
            RandomDistributionUtils.logger.debug("Running [{}]th iteration of solution search for new distribution", iterations);
            var hasNegativeElements = false;
            var newDistribution = findSimilarSuitableDistributionByLeastSquares(distribution, riskArray, totalRisk, negativeIndexes);
            for (int i = 0; i < distribution.length; i++) {
                if(newDistribution[i] < 0.0) {
                    hasNegativeElements = true;
                    negativeIndexes.add(i);
                }
            }
            if(!hasNegativeElements) {
                return new ImmutableTuple<>(true, newDistribution);
            } else {
                iterations++;
            }
        }
        logger.warn("System of linear equations for action distribution [" + Arrays.toString(distribution) +
            "] and risk array: [" + Arrays.toString(riskArray) + "] with totalRisk: [" + totalRisk + "] has no solution");
        return new ImmutableTuple<>(false, new double[0]);
    }

    private static double[] findSimilarSuitableDistributionByLeastSquares(double[] distribution, double[] riskArray, double totalRisk, Set<Integer> negativeIndexes) {

        RandomDistributionUtils.logger.debug("negative indexes: [{}]", negativeIndexes.toString());

        int distributionSize = distribution.length;
        int helpVariableCount = 2;
        int negativeHelpVariableCount = negativeIndexes.size();
        int automaticColumnCount = distributionSize + negativeHelpVariableCount;
        int totalColumnCount = helpVariableCount + automaticColumnCount;


        double[][] lhsArray = new double[totalColumnCount][totalColumnCount];
        double[] rhsArray = new double[totalColumnCount];

        // A
        for (int i = 0; i < distributionSize; i++) {
            for (int j = 0; j < distributionSize; j++) {
                lhsArray[i][j] = i == j ? 2 : 0;
            }
            for (int j = 0; j < negativeHelpVariableCount; j++) {
                lhsArray[i][j + distributionSize] = negativeIndexes.contains(i) ? -1 : 0;
            }
            lhsArray[i][automaticColumnCount] = -1 ;
            lhsArray[i][automaticColumnCount + 1] = -riskArray[i];
            rhsArray[i] = 2 * distribution[i];
        }

        // D
        int negativeIndexesAdded = 0;
        for (Integer negativeIndex : negativeIndexes) {
            for (int j = 0; j < distributionSize; j++) {
                lhsArray[distributionSize + negativeIndexesAdded][negativeIndex] = 1;
            }
            rhsArray[distributionSize + negativeIndexesAdded] = 0;
            negativeIndexesAdded++;
        }


        // B
        for (int i = 0; i < distributionSize; i++) {
            lhsArray[automaticColumnCount][i] = 1;
        }
        rhsArray[automaticColumnCount] = 1;


        // C
        for (int i = 0; i < distributionSize; i++) {
            lhsArray[automaticColumnCount + 1][i] = riskArray[i];
        }
        rhsArray[automaticColumnCount + 1] = totalRisk;

        try {
            RealMatrix realMatrix = new BlockRealMatrix(lhsArray);
            SingularValueDecomposition singularValueDecomposition = new SingularValueDecomposition(realMatrix);
//            LUDecomposition luDecomposition = new LUDecomposition(realMatrix);
//            DecompositionSolver solver = luDecomposition.getSolver();
            DecompositionSolver solver = singularValueDecomposition.getSolver();
            RealVector resultVector = solver.solve(new ArrayRealVector(rhsArray));

            var result = resultVector.toArray();

            var newDistribution = new double[distributionSize];
            for (int i = 0; i < distributionSize; i++) {
                newDistribution[i] = result[i];
            }
            hardRoundDistribution(newDistribution);
            return newDistribution;
        } catch(Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error occurred during solving linear equations");
            sb.append(System.lineSeparator());
            sb.append("OriginalDistribution: ");
            sb.append(Arrays.toString(distribution));
            sb.append(System.lineSeparator());
            sb.append("Risk reachability: ");
            sb.append(Arrays.toString(riskArray));
            sb.append(System.lineSeparator());
            sb.append("Total risk allowed: ");
            sb.append(totalRisk);
            sb.append(System.lineSeparator());
            sb.append("Indexes of negative variables: ");
            sb.append(negativeIndexes.toString());
            sb.append(System.lineSeparator());
            sb.append("Right side of equations: ");
            sb.append(Arrays.toString(rhsArray));
            sb.append(System.lineSeparator());
            sb.append("Left side of equations: ");
            sb.append(Arrays.deepToString(lhsArray));
            sb.append(System.lineSeparator());
            throw new IllegalStateException(sb.toString(), e);
        }
    }

}
