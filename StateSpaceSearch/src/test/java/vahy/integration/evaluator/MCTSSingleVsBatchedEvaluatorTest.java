package vahy.integration.evaluator;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.TrainableApproximator;
import vahy.impl.predictor.tensorflow.TensorflowTrainablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.tensorflow.TFHelper;
import vahy.tensorflow.TFModelImproved;
import vahy.test.ConvergenceAssert;
import vahy.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class MCTSSingleVsBatchedEvaluatorTest {

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPlayerSupplier(int batchSize, ProblemConfig config, SystemConfig systemConfig, int modelInputSize, int totalEntityCount) throws IOException, InterruptedException {

        var playerId = 1;
        double discountFactor = 1;

        var path_ = Paths.get(MCTSSingleVsBatchedEvaluatorTest.class.getClassLoader().getResource("tfModelPrototypes/create_value_vectorized_model.py").getPath());

        var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getPythonVirtualEnvPath(), systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, 0);
        var tfModel_ = new TFModelImproved(
            modelInputSize,
            totalEntityCount,
            65536,
            10,
            0.8,
            0.1,
            tfModelAsBytes_,
            systemConfig.getParallelThreadsCount(),
            new SplittableRandom(systemConfig.getRandomSeed()));

        var trainablePredictor = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
        var dataAggregator = new ReplayBufferDataAggregator(1000);
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState>(discountFactor, playerId, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        return new MCTSPolicyDefinitionSupplier<SHAction, SHState>(SHAction.class, 2, config).getPolicyDefinition(
            playerId,
            1,
            () -> 0.2,
            1,
            10,
            predictorTrainingSetup,
            batchSize
        );
    }

    private List<Double> runExperiment(SHConfig config, long seed, int batchSize) throws IOException, InterruptedException {


        var algorithmConfig = new CommonAlgorithmConfigBase(10, 50);

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            50,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python");

        var instance = new SHInstanceSupplier(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        var modelInputSize = instance.getInGameEntityObservation(1).getObservedVector().length;

        var policy = getPlayerSupplier(batchSize, config, systemConfig, modelInputSize, 2);

        var roundBuilder = RoundBuilder.getRoundBuilder("MCTSSingleVsBatchedTest", config, systemConfig, algorithmConfig, List.of(policy), SHInstanceSupplier::new);
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(x -> x.getTotalPayoffAverage().get(policy.getPolicyId())).collect(Collectors.toList());
    }

    @Test
    public void singleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(3);
        var trialCount = 4;
        try {
            var config = new SHConfigBuilder()
                .isModelKnown(true)
                .reward(100)
                .gameStringRepresentation(SHInstance.BENCHMARK_12)
                .maximalStepCountBound(100)
                .stepPenalty(1)
                .trapProbability(0.1)
                .buildConfig();

            var list = new ArrayList<List<Double>>();
            for (Long seed : (Iterable<Long>)seedStream::iterator) {
                List<Double> result = runExperiment(config, seed, 0);
                for (int i = 1; i <= trialCount; i++) {
                    List<Double> tmp = runExperiment(config, seed, i);
                    assertIterableEquals(result, tmp);
                }
                list.add(result);
                if(list.stream().map(List::hashCode).distinct().count() > 1) {
                    break;
                }
            }
            assertNotEquals(1, list.stream().map(List::hashCode).distinct().count());
        } catch (IOException | InterruptedException e) {
            Assertions.fail(e);
        }
    }

}
