package vahy.impl.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.AbstractMonteCarloTrainer;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirstVisitMontecarloTrainer<TAction extends Action, TReward extends Reward, TObservation extends Observation> extends AbstractMonteCarloTrainer<TAction, TReward, TObservation> {

    private final double discountFactor;

    public FirstVisitMontecarloTrainer(InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier,
                                       TrainablePolicySupplier<TAction, TReward, TObservation> trainablePolicySupplier,
                                       PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier,
                                       RewardAggregator<TReward> rewardAggregator,
                                       double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<State<TAction, TReward, TObservation>, TReward> calculatedVisitedRewards(Episode<TAction, TReward, TObservation> episode) {
        Map<State<TAction, TReward, TObservation>, TReward> firstVisitSet = new LinkedHashMap<>();
        List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> episodeStepHistoryList = episode.getEpisodeStepHistoryList();
        for (int i = 0; i < episode.getEpisodeStepHistoryList().size(); i++) {
            TReward aggregated = rewardAggregator.aggregateDiscount(episodeStepHistoryList.stream().skip(i).map(StateRewardReturn::getReward), discountFactor);
            if(!firstVisitSet.containsKey(episode.getEpisodeStepHistoryList().get(i).getState())) {
                firstVisitSet.put(episode.getEpisodeStepHistoryList().get(i).getState(), aggregated);
            }
        }
        return firstVisitSet;
    }
}
