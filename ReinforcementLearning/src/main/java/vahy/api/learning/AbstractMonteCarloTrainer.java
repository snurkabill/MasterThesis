package vahy.api.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.model.reward.VectorialReward;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.RolloutGameSampler;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.MutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

public abstract class AbstractMonteCarloTrainer<TAction extends Action, TReward extends VectorialReward, TObservation extends DoubleVectorialObservation> extends AbstractTrainer { // TODO: make observation and reward more abstract

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
        List<TObservation> observationList = new ArrayList<>();
        List<TReward> averagedRewardList = new ArrayList<>();
        for (Map.Entry<State<TAction, TReward, TObservation>, MutableTuple<Integer, TReward>> entry : visitAverageRewardMap.entrySet()) {
            observationList.add(entry.getKey().getObservation());
            averagedRewardList.add(entry.getValue().getSecond());
        }
        double[][] inputs = new double[observationList.size()][];
        double[][] targets = new double[averagedRewardList.size()][];
        for (int i = 0; i < observationList.size(); i++) {
            inputs[i] = observationList.get(i).getObservedVector();
            targets[i] = averagedRewardList.get(i).getAsVector();
        }
        trainablePolicySupplier.getTrainableStateEvaluator().fit(inputs, targets);
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
