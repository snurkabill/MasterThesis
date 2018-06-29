package vahy.api.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.RolloutGameSampler;
import vahy.utils.MutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public abstract class AbstractMonteCarloTrainer<TAction extends Action, TReward extends Reward, TObservation extends Observation> extends AbstractTrainer {

    private final RolloutGameSampler<TAction, TReward, TObservation> rolloutGameSampler;
    private final TrainablePolicySupplier<TAction, TReward, TObservation> trainablePolicySupplier;
    private final Map<State<TAction, TReward, TObservation>, MutableTuple<Integer, TReward>> visitAverageRewardMap = new LinkedHashMap<>();
    protected final RewardAggregator<TReward> rewardAggregator;

    public AbstractMonteCarloTrainer(InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier,
                                     TrainablePolicySupplier<TAction, TReward, TObservation> trainablePolicySupplier,
                                     PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier,
                                     RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
        this.rolloutGameSampler = new RolloutGameSampler<>(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier);
        this.trainablePolicySupplier = trainablePolicySupplier;
    }

    @Override
    public void trainPolicy(IntSupplier episodeCount) {
        List<Episode<TAction, TReward, TObservation>> episodeHistoryList = rolloutGameSampler.sampleEpisodes(episodeCount.getAsInt());
        for (Episode<TAction, TReward, TObservation> entry : episodeHistoryList) {
            addVisitedRewards(calculatedVisitedRewards(entry));
        }
        List<List<TObservation>> observationList = new ArrayList<>();
        List<TReward> averagedRewardList = new ArrayList<>();
        for (Map.Entry<State<TAction, TReward, TObservation>, MutableTuple<Integer, TReward>> entry : visitAverageRewardMap.entrySet()) {
            observationList.add(new ArrayList<>());
            observationList.get(observationList.size() - 1).add(entry.getKey().getObservation());
            averagedRewardList.add(entry.getValue().getSecond());
        }
        trainablePolicySupplier.getTrainableStateValueEvaluator().fit(observationList, averagedRewardList);
    }

    protected abstract Map<State<TAction, TReward, TObservation>, TReward> calculatedVisitedRewards(Episode<TAction, TReward, TObservation> episode);

    protected void addVisitedRewards(Map<State<TAction, TReward, TObservation>, TReward> sampledStateVisitMap) {
        for (Map.Entry<State<TAction, TReward, TObservation>, TReward> entry : sampledStateVisitMap.entrySet()) {
            if(visitAverageRewardMap.containsKey(entry.getKey())) {
                MutableTuple<Integer, TReward> integerTRewardMutableTuple = visitAverageRewardMap.get(entry.getKey());
                TReward averagedReward = rewardAggregator.averageReward(integerTRewardMutableTuple.getSecond(), integerTRewardMutableTuple.getFirst(), entry.getValue());
                integerTRewardMutableTuple.setFirst(integerTRewardMutableTuple.getFirst() + 1);
                integerTRewardMutableTuple.setSecond(averagedReward);
            } else {
                visitAverageRewardMap.put(entry.getKey(), new MutableTuple<>(1, entry.getValue()));
            }
        }
    }
}
