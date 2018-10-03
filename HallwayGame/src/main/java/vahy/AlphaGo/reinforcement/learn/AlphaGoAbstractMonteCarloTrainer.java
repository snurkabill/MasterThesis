package vahy.AlphaGo.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoEpisode;
import vahy.AlphaGo.reinforcement.episode.AlphaGoRolloutGameSampler;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;
import vahy.utils.MutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AlphaGoAbstractMonteCarloTrainer {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoAbstractMonteCarloTrainer.class.getName());

    private final AlphaGoRolloutGameSampler rolloutGameSampler;
    private final AlphaGoTrainablePolicySupplier trainablePolicySupplier;
    private final Map<DoubleVectorialObservation, MutableTuple<Integer, MutableTuple<DoubleScalarReward, double[]>>> visitAverageRewardMap = new LinkedHashMap<>();
    protected final DoubleScalarRewardAggregator rewardAggregator;

    public AlphaGoAbstractMonteCarloTrainer (HallwayGameInitialInstanceSupplier initialStateSupplier,
                                             AlphaGoTrainablePolicySupplier trainablePolicySupplier,
                                             AlphaGoEnvironmentPolicySupplier opponentPolicySupplier,
                                             DoubleScalarRewardAggregator rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
        this.rolloutGameSampler = new AlphaGoRolloutGameSampler(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier);
        this.trainablePolicySupplier = trainablePolicySupplier;
    }

    public void trainPolicy(int episodeCount) {
        logger.info("Starting MonteCarlo training on [{}] episodeCount", episodeCount);
        List<AlphaGoEpisode> episodeHistoryList = rolloutGameSampler.sampleEpisodes(episodeCount);
        for (AlphaGoEpisode entry : episodeHistoryList) {
            addVisitedRewards(calculatedVisitedRewards(entry));
        }
        logger.debug("Sampled all episodes");
        List<ImmutableTuple<DoubleVectorialObservation, double[]>> observationRewardList = new ArrayList<>();
        for (Map.Entry<DoubleVectorialObservation, MutableTuple<Integer, MutableTuple<DoubleScalarReward, double[]>>> entry : visitAverageRewardMap.entrySet()) {
            double[] probabilities = entry.getValue().getSecond().getSecond();
            double[] outputVector = new double[probabilities.length + 1];
            outputVector[0] = entry.getValue().getSecond().getFirst().getValue();
            for (int i = 0; i < probabilities.length; i++) {
                outputVector[i + 1] = probabilities[i];
//                outputVector[i + 1] = 0.33333333d;
            }
            observationRewardList.add(new ImmutableTuple<>(entry.getKey(), outputVector));
        }
        trainablePolicySupplier.train(observationRewardList);
//        double[] asdf1 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(0).getFirst());
//        double[] asdf2 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(1).getFirst());
//        double[] asdf3 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(2).getFirst());
//        double[] asdf4 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(3).getFirst());
        logger.debug("training iteration finished");
    }

    protected abstract Map<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> calculatedVisitedRewards(AlphaGoEpisode episode);

    protected void addVisitedRewards(Map<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> sampledStateVisitMap) {
        for (Map.Entry<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableTuple<Integer, MutableTuple<DoubleScalarReward, double[]>> integerTRewardMutableTuple = visitAverageRewardMap.get(entry.getKey());
                DoubleScalarReward averagedReward = rewardAggregator.averageReward(integerTRewardMutableTuple.getSecond().getFirst(), integerTRewardMutableTuple.getFirst(), entry.getValue().getFirst());
                double[] runningAverageProbabilities = integerTRewardMutableTuple.getSecond().getSecond();
                int countOfAlreadyAveraged = integerTRewardMutableTuple.getFirst();
                double[] newProbabilities = entry.getValue().getSecond();
                for (int i = 0; i < runningAverageProbabilities.length; i++) {
                    runningAverageProbabilities[i] = (runningAverageProbabilities[i] * countOfAlreadyAveraged + newProbabilities[i]) / (countOfAlreadyAveraged + 1);
                }
                integerTRewardMutableTuple.setFirst(integerTRewardMutableTuple.getFirst() + 1);
                integerTRewardMutableTuple.getSecond().setFirst(averagedReward);
                integerTRewardMutableTuple.getSecond().setSecond(runningAverageProbabilities);
            } else {
                visitAverageRewardMap.put(entry.getKey(), new MutableTuple<>(1, new MutableTuple<>(entry.getValue().getFirst(), entry.getValue().getSecond())));
            }
        }
    }
}
