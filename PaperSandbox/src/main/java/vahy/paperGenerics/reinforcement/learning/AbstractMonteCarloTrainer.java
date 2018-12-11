package vahy.paperGenerics.reinforcement.learning;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.reward.RewardAggregator;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperNodeEvaluator;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMonteCarloTrainer<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends PaperMetadata<TAction, DoubleReward>,
    TState extends PaperState<TAction, DoubleReward, DoubleVector, TState>>
    extends AbstractTrainer<TAction, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMonteCarloTrainer.class.getName());
    private final Map<DoubleVector, MutableDataSample> visitAverageRewardMap = new LinkedHashMap<>();

    public AbstractMonteCarloTrainer(InitialStateSupplier<TAction, DoubleReward, DoubleVector, TState> initialStateSupplier,
                                     TrainablePaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                                     PaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> opponentPolicySupplier,
                                     PaperNodeEvaluator paperNodeEvaluator,
                                     double discountFactor,
                                     RewardAggregator<DoubleReward> rewardAggregator,
                                     int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, paperNodeEvaluator, discountFactor, rewardAggregator, stepCountLimit);
    }

    @Override
    public void trainPolicy(int episodeCount) {
        logger.info("Starting MonteCarlo training on [{}] episodeCount", episodeCount);
        List<EpisodeResults<TAction, DoubleReward, DoubleVector, TState>> paperEpisodeHistoryList = this.getGameSampler().sampleEpisodes(episodeCount);
        for (EpisodeResults<TAction, DoubleReward, DoubleVector, TState> entry : paperEpisodeHistoryList) {
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

    protected abstract Map<DoubleVector, MutableDataSample> calculatedVisitedRewards(EpisodeResults<TAction, DoubleReward, DoubleVector, TState> paperEpisode);

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