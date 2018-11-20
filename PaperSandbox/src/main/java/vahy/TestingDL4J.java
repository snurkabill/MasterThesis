package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.reinforcement.learn.tf.TFModel;

import java.io.File;
import java.util.Arrays;
import java.util.SplittableRandom;

public class TestingDL4J {

    private static final Logger logger = LoggerFactory.getLogger(TestingDL4J.class);

    public static void main(String[] args) {

//
//        double[] vector1 = new double[] {0};
//        double[] vector2 = new double[] {1};
//        double[] vector3 = new double[] {2};
//        double[] vector4 = new double[] {3};

        double[] vector1 = new double[] {1.0, 0.0, 0.0, 0.0};
        double[] vector2 = new double[] {0.0, 1.0, 0.0, 0.0};
        double[] vector3 = new double[] {0.0, 0.0, 1.0, 0.0};
        double[] vector4 = new double[] {0.0, 0.0, 0.0, 1.0};


        double[] target1 = new double[] {96.0, 0.9, 0.02, 0.49, 0.49};
        double[] target2 = new double[] {97.0, 0.6, 0.01, 0.01, 0.98};
        double[] target3 = new double[] {98.0, 0.4, 0.01, 0.98, 0.01};
        double[] target4 = new double[] {99.0, 0.1, 0.98, 0.01, 0.01};

        double[][] input = new double[4][];
        double[][] target = new double[4][];

        input[0] = vector1;
        input[1] = vector2;
        input[2] = vector3;
        input[3] = vector4;

        target[0] = target1;
        target[1] = target2;
        target[2] = target3;
        target[3] = target4;


        File tfGraphFile = new File(TestingDL4J.class.getClassLoader().getResource("tfModel/graph.pb").getFile());

//        Dl4jModel asdf = new Dl4jModel(4, 5, null, 0, 0.01, 100);
        TFModel asdf = new TFModel(4, 5, 100, 1, tfGraphFile, new SplittableRandom());

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 4; j++) {
                logger.info(Arrays.stream(asdf.predict(input[j])).mapToObj(Double::toString).reduce((left, right) -> left + ", " + right).get());
            }
            logger.info("TRAINING [{}]", i);
            long start = System.currentTimeMillis();
            asdf.fit(input, target);
            long end = System.currentTimeMillis();
            logger.info("Training took: [{}] ms", end - start);
//            start = System.currentTimeMillis();
//            for (int k = 0; k < 1000; k++) {
//                for (int j = 0; j < 4; j++) {
//                    logger.debug(Arrays.stream(asdf.predict(input[j])).mapToObj(Double::toString).reduce((left, right) -> left + ", " + right).get());
//                }
//            }
            for (int j = 0; j < 4; j++) {
                logger.info(Arrays.stream(asdf.predict(input[j])).mapToObj(Double::toString).reduce((left, right) -> left + ", " + right).get());
            }
            end = System.currentTimeMillis();
            logger.info("Eval took: [{}] ms", end - start);

        }
    }
}
