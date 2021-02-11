package vahy.integration.evaluator;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManInstanceInitializer;
import vahy.examples.bomberman.BomberManState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.ReplayBufferDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.alphazero.AlphaZeroDataMaker_V1;
import vahy.impl.policy.alphazero.AlphaZeroPolicyDefinitionSupplier;
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

public class AlphaZeroSingleVsBatchedEvaluatorTest {

    private List<PolicyDefinition<BomberManAction, DoubleVector, BomberManState>> getPlayerSupplierList(int playerCount, int envEntitiesCount, int batchSize, ProblemConfig config, SystemConfig systemConfig, int modelInputSize) throws IOException, InterruptedException {

        var playerList = new ArrayList<PolicyDefinition<BomberManAction, DoubleVector, BomberManState>>(playerCount);

        var totalEntityCount = envEntitiesCount + playerCount;
        var actionClass = BomberManAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;

        double discountFactor = 1;

        for (int i = 0; i < playerCount; i++) {
            int playerId = i + envEntitiesCount;

            var path_ = Paths.get(AlphaZeroSingleVsBatchedEvaluatorTest.class.getClassLoader().getResource("tfModelPrototypes/create_alphazero_prototype.py").getPath());

            var tfModelAsBytes_ = TFHelper.loadTensorFlowModel(path_, systemConfig.getRandomSeed(), modelInputSize, totalEntityCount, totalActionCount);
            var tfModel_ = new TFModelImproved(
                modelInputSize,
                totalEntityCount + totalActionCount,
                32768,
                1,
                0.8,
                0.1,
                tfModelAsBytes_,
                systemConfig.getParallelThreadsCount(),
                new SplittableRandom(systemConfig.getRandomSeed()),
                false);

            var trainablePredictor = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel_));
            var dataAggregator = new ReplayBufferDataAggregator(1000);
            var episodeDataMaker = new AlphaZeroDataMaker_V1<BomberManAction, BomberManState>(playerId, totalActionCount, discountFactor, dataAggregator);


            var predictorTrainingSetup = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManState>(
                playerId,
                trainablePredictor,
                episodeDataMaker,
                dataAggregator
            );
            playerList.add(new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManState>(BomberManAction.class, totalEntityCount, config).getPolicyDefinition(
                playerId,
                1,
                1,
                () -> 0.2,
                10,
                predictorTrainingSetup,
                batchSize
            ));
        }
        return playerList;

    }

    private List<List<Double>> runExperiment(BomberManConfig config, long seed, int batchSize) throws IOException, InterruptedException {

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
            Path.of("TEST_PATH"));

        var instance = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        var modelInputSize = instance.getInGameEntityObservation(5).getObservedVector().length;

        var policyList = getPlayerSupplierList(config.getPlayerCount(), config.getEnvironmentPolicyCount(), batchSize, config, systemConfig, modelInputSize);

        var roundBuilder = RoundBuilder.getRoundBuilder("AlphaZeroSingleVsBatchTest", config, systemConfig, algorithmConfig, policyList, BomberManInstanceInitializer::new);
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(EpisodeStatistics::getTotalPayoffAverage).collect(Collectors.toList());
    }


    @Test
    public void singleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(3);
        var trialCount = 4;
        var playerCount = 3;
        try {
            var config = new BomberManConfig(
                100,
                true,
                100,
                1,
                3,
                3,
                3,
                1,
                playerCount,
                0.1,
                BomberManInstance.BM_01,
                PolicyShuffleStrategy.CATEGORY_SHUFFLE);

            var list = new ArrayList<List<List<Double>>>();
            for (Long seed : (Iterable<Long>)seedStream::iterator) {
                List<List<Double>> result = runExperiment(config, seed, 0);
                for (int i = 1; i <= trialCount; i++) {
                    List<List<Double>> tmp = runExperiment(config, seed, i);
                    for (int j = 0; j < tmp.size(); j++) {
                        assertIterableEquals(result.get(j), tmp.get(j));
                    }
                }
                list.add(result);
                if(list.stream().map(List::hashCode).distinct().count() > 1) {
                    break;
                }
            }
            assertNotEquals(1, list.stream().map(List::hashCode).distinct().count());
        } catch (IOException | InvalidInstanceSetupException | InterruptedException e) {
            Assertions.fail(e);
        }
    }

}
