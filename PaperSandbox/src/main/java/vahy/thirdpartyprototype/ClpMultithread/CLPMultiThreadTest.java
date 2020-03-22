package vahy.thirdpartyprototype.ClpMultithread;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CLPMultiThreadTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        int seed = 0;          // random number seed

        // set up and seed the random number generator
        Random rng = new Random(seed);
        // generate the cost matrix

        long time = System.currentTimeMillis();

        for (int dim = 50; dim < 70; dim++) {
            System.out.println("Dimension [" + dim + "]");
            double[][] cost = new double[dim][dim];
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    cost[i][j] = rng.nextDouble();
                }
            }

            double[][] cost2 = new double[dim][dim];
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    cost2[i][j] = rng.nextDouble();
                }
            }

            double[][] cost3 = new double[dim][dim];
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    cost3[i][j] = rng.nextDouble();
                }
            }

            double[][] cost4 = new double[dim][dim];
            for (int i = 0; i < dim; i++) {
                for (int j = 0; j < dim; j++) {
                    cost4[i][j] = rng.nextDouble();
                }
            }

            var singleThreaded = new Worker(cost, -1);

            singleThreaded.run();
            double referenceSolution = singleThreaded.getSolution();
            double[][] referenceSolutionArr = singleThreaded.getAssigemment();

            var singleThreaded2 = new Worker(cost, -2);
            singleThreaded2.run();
            double referenceSolution2 = singleThreaded2.getSolution();
            double[][] referenceSolutionArr2 = singleThreaded2.getAssigemment();

            test(referenceSolution, referenceSolution2);
            test(referenceSolutionArr, referenceSolutionArr2, dim);


            var list = new ArrayList<Worker>();
            for (int i = 0; i < 1000; i += 4) {
                list.add(new Worker(cost, i));
                list.add(new Worker(cost2, i + 1));
                list.add(new Worker(cost3, i + 2));
                list.add(new Worker(cost4, i + 3));
            }
            ExecutorService executorService = Executors.newFixedThreadPool(50);
            List<Future<Worker>> futures = executorService.invokeAll(list);
            executorService.shutdown();

            for (int i = 0; i < futures.size(); i++) {
                if(i % 4 == 0) {
                    var testReference = futures.get(i).get();
                    double solution2 = testReference.getSolution();
                    double[][] solutionRef2 = testReference.getAssigemment();

                    test(referenceSolution, solution2);
                    test(referenceSolutionArr, solutionRef2, dim);
                }
            }

        }

        System.out.println("Millis: [" + (System.currentTimeMillis() - time) + "]");

    }



    public static void test(double solution, double solution2) {
        if(Math.abs(solution - solution2) > Math.pow(10, -10)) {
            throw new IllegalArgumentException("Diff [" + Math.abs(solution - solution2) + "]");
        }
    }

    public static void test(double[][] solution, double[][] solution2, int dim) {
        for (int j = 0; j < dim; j++) {
            for (int k = 0; k < dim; k++) {
                if(Math.abs(solution[j][k] - solution2[j][k]) > Math.pow(10, -10)) {
                    throw new IllegalArgumentException("Diff [" + Math.abs(solution[j][k] - solution2[j][k]) + "]");
                }
            }
        }
    }

}
