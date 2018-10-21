package vahy.paper.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMonteCarloTrainer extends AbstractTrainer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMonteCarloTrainer.class.getName());


    private final Map<DoubleVectorialObservation, MutableDataSample> visitAverageRewardMap = new LinkedHashMap<>();


    public AbstractMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                                     PaperTrainablePolicySupplier paperTrainablePolicySupplier,
                                     EnvironmentPolicySupplier opponentPolicySupplier,
                                     double discountFactor,
                                     DoubleScalarRewardAggregator rewardAggregator) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator);
    }

    @Override
    public void trainPolicy(int episodeCount) {
        logger.info("Starting MonteCarlo training on [{}] episodeCount", episodeCount);
        List<PaperEpisode> paperEpisodeHistoryList = this.getGameSampler().sampleEpisodes(episodeCount);
        for (PaperEpisode entry : paperEpisodeHistoryList) {
            addVisitedRewards(calculatedVisitedRewards(entry));
        }
        logger.debug("Sampled all episodes");
        List<ImmutableTuple<DoubleVectorialObservation, double[]>> observationRewardList = new ArrayList<>();
        for (Map.Entry<DoubleVectorialObservation, MutableDataSample> entry : visitAverageRewardMap.entrySet()) {
            double[] outputVector = createOutputVector(entry.getValue());
            observationRewardList.add(new ImmutableTuple<>(entry.getKey(), outputVector));
        }
        trainPolicy(observationRewardList);
//        double[] asdf1 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(0).getFirst());
//        double[] asdf2 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(1).getFirst());
//        double[] asdf3 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(2).getFirst());
//        double[] asdf4 = trainablePolicySupplier.getTrainableRewardApproximator().apply(observationRewardList.get(3).getFirst());
        logger.debug("training iteration finished");
    }

    protected abstract Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode);

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
