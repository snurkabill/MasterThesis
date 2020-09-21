package vahy.example.bomberman;

import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManInstanceInitializer;
import vahy.examples.bomberman.BomberManState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Example_random_reproducing {

    private Example_random_reproducing() {}

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException, InterruptedException {

        var config = new BomberManConfig(
            500,
            true,
            100,
            1,
            2,
            3,
            3,
            1,
            2,
            0.1,
            BomberManInstance.BM_01,
            PolicyShuffleStrategy.CATEGORY_SHUFFLE);
        var systemConfig = new SystemConfig(
            987567,
            false,
            1,
            true,
            2,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            System.getProperty("user.home") + "/.local/virtualenvs/tensorflow_2_0/bin/python");

        var algorithmConfig = new CommonAlgorithmConfigBase(1, 10);

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var treeExpansionCount = 10;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        // MCTS WITH APPROXIMATOR




//        var trainablePredictorMCTSEval_1 = new TrainableApproximator(tfModel);
        var defPred = new double[totalEntityCount];
        Arrays.fill(defPred, 10.0);
        var trainablePredictorMCTSEval_1 = new DataTablePredictorWithLr(defPred, 0.1);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState>(discountFactor, environmentPolicyCount + 0);
//        var dataAggregatorMCTSEval_1 = new ReplayBufferDataAggregator(1000);
        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 0,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );

        var batchEvalSize = 3;

        var mctsSupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);
        var mctsPlayer_1 = mctsSupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1, batchEvalSize);


        var policyArgumentsList = IntStream.of(1).mapToObj(x -> new PolicyDefinition<BomberManAction, DoubleVector, BomberManState>(
            environmentPolicyCount + x,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<BomberManAction, DoubleVector, BomberManState>(random, environmentPolicyCount + x),
            new ArrayList<>())).collect(Collectors.toList());

//        policyArgumentsList.add(0, valuePolicyPlayer_1);
//        policyArgumentsList.add(0, alphaGoPlayer_1);
        policyArgumentsList.add(0, mctsPlayer_1);



        var roundBuilder = RoundBuilder.getRoundBuilder("BomberManExample01", config, systemConfig, algorithmConfig, policyArgumentsList, BomberManInstanceInitializer::new);

        var start = System.currentTimeMillis();
        var result = roundBuilder.execute();
        var end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + "[ms]");

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));


        List<Double> totalPayoffAverage = result.getEvaluationStatistics().getTotalPayoffAverage();

        for (int i = 0; i < totalPayoffAverage.size(); i++) {
            System.out.println("Policy" + i + " result: " + totalPayoffAverage.get(i));
        }

    }

}
