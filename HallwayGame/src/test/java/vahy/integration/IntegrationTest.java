package vahy.integration;

import org.testng.annotations.Test;
import vahy.game.NotValidGameStringRepresentationException;

import java.io.IOException;

public class IntegrationTest {

    @Test
    public void MonteCarloSimpleTest() throws IOException, NotValidGameStringRepresentationException {
//        ClassLoader classLoader = IntegrationTest.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway_demo3.txt");
//        File file = new File(url.getFile());
//
//        SplittableRandom random = new SplittableRandom(0);
//        GameConfig gameConfig = new ConfigBuilder().reward(1000).noisyMoveProbability(0.0).stepPenalty(1).trapProbability(1).buildConfig();
//        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
//
//        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
//        double discountFactor = 0.9; // 0.9
//        int rolloutCount = 20;
//
//
//        PaperMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> searchNodeMetadataFactory = new PaperMetadataFactory<>(rewardAggregator);
//        PaperNodeSelector<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> nodeSelector = new PaperNodeSelector<>(5, random);
//        PaperTreeUpdater<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl> paperTreeUpdater = new PaperTreeUpdater<>();
//        NodeEvaluator<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, MonteCarloTreeSearchMetadata<DoubleReward>, HallwayStateImpl> nodeEvaluator = new MonteCarloEvaluator<>(
//            new SearchNodeBaseFactoryImpl<>(new MonteCarloTreeSearchMetadataFactory<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>(rewardAggregator)),
//            random,
//            rewardAggregator,
//            discountFactor,
//            rolloutCount);
//
//
//        PaperPolicySupplier<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> policySupplier =
//            new PaperPolicySupplier<>(
//                HallwayAction.class,
//                searchNodeMetadataFactory,
//                0.0,
//                random,
//                nodeSelector,
//                nodeEvaluator,
//                paperTreeUpdater,
//                () -> new TreeUpdateConditionSuplierCountBased(10000));
//
//        EpisodeGameSampler<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, PaperMetadata<HallwayAction, DoubleReward>, HallwayStateImpl> sampler = new EpisodeGameSampler<>(
//            hallwayGameInitialInstanceSupplier,
//            policySupplier,
//            new EnvironmentPolicySupplier(random),
//            1000
//        );
//        List<List<Double>> rewardHistory = sampler
//            .sampleEpisodes(10)
//            .stream()
//            .map(x -> x
//                .getEpisodeHistoryList()
//                .stream()
//                .map(y -> y.getFirst().getReward().getValue())
//                .collect(Collectors.toList()))
//            .collect(Collectors.toList());
//        System.out.println(rewardHistory.stream().mapToInt(List::size).average());
//        Assert.assertTrue(rewardHistory.stream().allMatch(x -> x.size() == 17));
    }
}
