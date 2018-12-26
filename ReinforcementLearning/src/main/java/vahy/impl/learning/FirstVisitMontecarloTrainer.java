package vahy.impl.learning;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.AbstractMonteCarloTrainer;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirstVisitMontecarloTrainer<
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractMonteCarloTrainer<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    private final double discountFactor;

    public FirstVisitMontecarloTrainer(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                       TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> trainablePolicySupplier,
                                       PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicySupplier,
                                       RewardAggregator<TReward> rewardAggregator,
                                       double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<TState, TReward> calculatedVisitedRewards(Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episode) {
        Map<TState, TReward> firstVisitSet = new LinkedHashMap<>();
        List<StateActionReward<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> episodeHistory = episode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getState().isOpponentTurn()) {
                if(!firstVisitSet.containsKey(episodeHistory.get(i).getState())) {
                    TReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(StateActionReward::getReward), discountFactor);
                    firstVisitSet.put(episodeHistory.get(i).getState(), aggregated);
                }
            }
        }
        return firstVisitSet;
    }
}
