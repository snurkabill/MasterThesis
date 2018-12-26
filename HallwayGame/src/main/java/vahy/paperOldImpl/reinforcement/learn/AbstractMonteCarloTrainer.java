package vahy.paperOldImpl.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperOldImpl.policy.EnvironmentPolicySupplier;
import vahy.paperOldImpl.policy.PaperTrainablePaperPolicySupplier;
import vahy.paperOldImpl.reinforcement.episode.PaperEpisode;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMonteCarloTrainer extends AbstractTrainer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMonteCarloTrainer.class.getName());


    private final Map<DoubleVector, MutableDataSample> visitAverageRewardMap = new LinkedHashMap<>();


    public AbstractMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                                     PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier,
                                     EnvironmentPolicySupplier opponentPolicySupplier,
                                     double discountFactor,
                                     DoubleScalarRewardAggregator rewardAggregator,
                                     int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator, stepCountLimit);
    }

    @Override
    public void trainPolicy(int episodeCount) {
        logger.info("Starting MonteCarlo training on [{}] episodeCount", episodeCount);
        List<PaperEpisode> paperEpisodeHistoryList = this.getGameSampler().sampleEpisodes(episodeCount);
        for (PaperEpisode entry : paperEpisodeHistoryList) {
            addVisitedRewards(calculatedVisitedRewards(entry));
        }
        logger.debug("Sampled all episodes");
        List<ImmutableTuple<DoubleVector, double[]>> observationRewardList = new ArrayList<>();
        for (Map.Entry<DoubleVector, MutableDataSample> entry : visitAverageRewardMap.entrySet()) {
            double[] outputVector = createOutputVector(entry.getValue());
            observationRewardList.add(new ImmutableTuple<>(entry.getKey(), outputVector));
        }
        logger.info("Training policy on [{}] samples", observationRewardList.size());
        trainPolicy(observationRewardList);
        logger.debug("training iteration finished");
    }

    protected abstract Map<DoubleVector, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode);

    protected void addVisitedRewards(Map<DoubleVector, MutableDataSample> sampledStateVisitMap) {
        for (Map.Entry<DoubleVector, MutableDataSample> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableDataSample mutableDataSample = visitAverageRewardMap.get(entry.getKey());
                mutableDataSample.addDataSample(entry.getValue());
            } else {
                visitAverageRewardMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
