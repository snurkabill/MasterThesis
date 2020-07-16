package vahy.integration.evaluator;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.integration.SH.AbstractSHConvergenceTest;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MCTSSingleVsBatchedEvaluatorTest extends AbstractSHConvergenceTest {

    private PolicyDefinition<SHAction, DoubleVector, SHState> getPlayerSupplier(int batchSize, ProblemConfig config) {

        var playerId = 1;
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0, 0.0}, 0.1);
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState>(discountFactor, playerId);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

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

    private List<Double> runExperiment(PolicyDefinition<SHAction, DoubleVector, SHState> policy, SHConfig config, long seed) {


        var algorithmConfig = new CommonAlgorithmConfigBase(50, 50);

        var systemConfig = new SystemConfig(
            seed,
            false,
            TEST_THREAD_COUNT,
            false,
            50,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var roundBuilder = getRoundBuilder(config, algorithmConfig, systemConfig, policy);
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(x -> x.getTotalPayoffAverage().get(policy.getPolicyId())).collect(Collectors.toList());
    }

    @Test
    public void singleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(10);
        var trialCount = 5;

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
            List<Double> result = runExperiment(getPlayerSupplier(0, config), config, seed);
            for (int i = 1; i <= trialCount; i++) {
                List<Double> tmp = runExperiment(getPlayerSupplier(i, config), config, seed);
                assertIterableEquals(result, tmp);
            }
            list.add(result);
        }
        assertNotEquals(1, list.stream().map(List::hashCode).distinct().count());
    }

}
