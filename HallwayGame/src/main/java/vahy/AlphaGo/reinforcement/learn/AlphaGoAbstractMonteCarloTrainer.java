package vahy.AlphaGo.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoEpisode;
import vahy.AlphaGo.reinforcement.episode.AlphaGoRolloutGameSampler;
import vahy.AlphaGo.tree.AlphaGoNodeEvaluator;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AlphaGoAbstractMonteCarloTrainer {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoAbstractMonteCarloTrainer.class.getName());

    private final AlphaGoRolloutGameSampler rolloutGameSampler;
    private final AlphaGoTrainablePolicySupplier trainablePolicySupplier;
    private final Map<DoubleVectorialObservation, MutableDataSample> visitAverageRewardMap = new LinkedHashMap<>();
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
        for (Map.Entry<DoubleVectorialObservation, MutableDataSample> entry : visitAverageRewardMap.entrySet()) {
            double[] probabilities = entry.getValue().getProbabilities();
            double[] outputVector = new double[probabilities.length + AlphaGoNodeEvaluator.POLICY_START_INDEX];
            outputVector[AlphaGoNodeEvaluator.Q_VALUE_INDEX] = entry.getValue().getReward().getValue();
            outputVector[AlphaGoNodeEvaluator.RISK_VALUE_INDEX] = entry.getValue().getRisk();
            for (int i = 0; i < probabilities.length; i++) {
                outputVector[i + AlphaGoNodeEvaluator.POLICY_START_INDEX] = probabilities[i];
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

    protected abstract Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(AlphaGoEpisode episode);

    protected void addVisitedRewards(Map<DoubleVectorialObservation, MutableDataSample> sampledStateVisitMap) {
        for (Map.Entry<DoubleVectorialObservation, MutableDataSample> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableDataSample mutableDataSample = visitAverageRewardMap.get(entry.getKey());
                mutableDataSample.addDataSample(entry.getValue());
            } else {
                visitAverageRewardMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
