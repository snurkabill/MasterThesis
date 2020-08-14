package vahy.tensorflow;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

@Fork(value = 3, jvmArgs = {"-Xms4G", "-Xmx4G"})
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class TFHugeModelLatencyBenchmark {

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
    }

    private TFModelImproved model;
    private double[] dummyInput_0;
    private double[] dummyInput_1;
//    private double[] dummyInput_2;
//    private double[] dummyInput_3;
//    private double[] dummyInput_4;

    @Setup
    public void setUp() throws IOException, InterruptedException {
        SplittableRandom random = new SplittableRandom(0);

        int inputDim = 2;
        int outputDim = 1;

        double[][] inputTrainData = {{0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, {1.0, 1.0}};
        double[][] targetTrainData = {{-1}, {-1}, {-1}, {1}};

        int trainingIterations_IN_MODEL = 1;
        int trainingIterations_OUTSIDE_MODEL = 1000;
        int batchSize = 1;

        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python";
//        String environmentPath = System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python";
        var modelPath = Paths.get(TFHugeModelLatencyBenchmark.class.getClassLoader().getResource("tfModelPrototypes/HUGE_MODEL.py").getPath());

        var modelRepresentation = TFHelper.loadTensorFlowModel(modelPath, environmentPath, random.nextLong(), inputDim, outputDim, 0);
        model = new TFModelImproved(inputDim, outputDim, batchSize, trainingIterations_IN_MODEL, 0.7, 0.0001, modelRepresentation, 1, random);

        for (int i = 0; i < trainingIterations_OUTSIDE_MODEL; i++) {
            TFHelper.trainingLoop(inputTrainData, targetTrainData, model);
        }

        dummyInput_0 = random.doubles(inputDim).toArray();
        dummyInput_1 = random.doubles(inputDim).toArray();
//        dummyInput_2 = random.doubles(inputDim).toArray();
//        dummyInput_3 = random.doubles(inputDim).toArray();
//        dummyInput_4 = random.doubles(inputDim).toArray();

    }

    @TearDown
    public void tearDown() {
        model.close();
    }

    @Benchmark
    public void singlePrediction_0(Blackhole bh) {
        bh.consume(model.predict(dummyInput_0));
    }

    @Benchmark
    public void singlePrediction_1(Blackhole bh) {
        bh.consume(model.predict(dummyInput_1));
    }
}
