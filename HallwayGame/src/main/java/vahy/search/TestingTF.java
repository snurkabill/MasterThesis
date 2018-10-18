package vahy.search;

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.Tensors;
import vahy.TestingDL4J;

import java.io.File;
import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestingTF {

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
        // double[][] target = new double[4][];

        input[0] = vector1;
        input[1] = vector2;
        input[2] = vector3;
        input[3] = vector4;

//        target[0] = target1;
//        target[1] = target2;
//        target[2] = target3;
//        target[3] = target4;

        double[][] target = new double[4][];

        target[0] = new double[] {1};
        target[1] = new double[] {2};
        target[2] = new double[] {3};
        target[3] = new double[] {4};

        File tfGraphFile = new File(TestingDL4J.class.getClassLoader().getResource("tfModel/graph.pb").getFile());
        Graph graph = new Graph();
        Session sess;

        sess = new Session(graph);
        try {
            graph.importGraphDef(Files.readAllBytes(Paths.get(tfGraphFile.getAbsolutePath())));
        } catch (IOException e) {
            throw new IllegalArgumentException("Tf model handling crashed. TODO: quite general exception", e);
        }
        sess.runner().addTarget("init").run();


        try (Tensor<Double> tfInput = Tensors.create(input)) {
            Tensor<Double> output = sess
                .runner()
                .feed("input_node", tfInput)
                .fetch("q_node_2")
                .run()
                .get(0)
                .expect(Double.class);
            double[] outputVector = new double[1];
            DoubleBuffer doubleBuffer = DoubleBuffer.wrap(outputVector);
            output.writeTo(doubleBuffer);

            System.out.println(outputVector[0]);
            output.close();  // needed?

        }

        try (
            Tensor<Double> tfInput = Tensors.create(input);
            Tensor<Double> tfQTarget = Tensors.create(target)
        ) {
            sess
                .runner()
                .feed("input_node", tfInput)
                .feed("Q_target_node", tfQTarget)
                .addTarget("train_node")
                .run();
        }

        try (Tensor<Double> tfInput = Tensors.create(input)) {
            Tensor<Double> output = sess
                .runner()
                .feed("input_node", tfInput)
                .fetch("prediction_node")
                .run()
                .get(0)
                .expect(Double.class);
            double[] outputVector = new double[1];
            DoubleBuffer doubleBuffer = DoubleBuffer.wrap(outputVector);
            output.writeTo(doubleBuffer);

            System.out.println(outputVector[0]);
            output.close();  // needed?

        }

        System.out.println("asdf");


    }

}
