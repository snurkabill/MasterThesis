package vahy.impl.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.AbstractMonteCarloTrainer;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EveryVisitMontecarloTrainer<TAction extends Action, TReward extends DoubleVectorialReward, TObservation extends DoubleVectorialObservation> extends AbstractMonteCarloTrainer<TAction, TReward, TObservation> {

    private final double discountFactor;

    public EveryVisitMontecarloTrainer(InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier,
                                       TrainablePolicySupplier<TAction, TReward, TObservation> trainablePolicySupplier,
                                       PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier,
                                       RewardAggregator<TReward> rewardAggregator,
                                       double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<State<TAction, TReward, TObservation>, TReward> calculatedVisitedRewards(Episode<TAction, TReward, TObservation> episode) {
        Map<State<TAction, TReward, TObservation>, List<TReward>> everyVisitMap = new LinkedHashMap<>();
        List<StateActionReward<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> episodeHistory = episode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            TReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(StateActionReward::getReward), discountFactor);
            if(!everyVisitMap.containsKey(episodeHistory.get(i).getState())) {
                everyVisitMap.put(episodeHistory.get(i).getState(), Collections.singletonList(aggregated));
            } else {
                List<TReward> rewardList = everyVisitMap.get(episodeHistory.get(i).getState());
                rewardList.add(aggregated);
            }
        }
        return everyVisitMap
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                x -> rewardAggregator.averageReward(x.getValue()))
            );
    }



}
