package vahy.AlphaGo.reinforcement.learn;

import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoEpisode;
import vahy.AlphaGo.reinforcement.episode.AlphaGoStepRecord;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.environment.ActionType;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlphaGoFirstVisitMonteCarloTrainer extends AlphaGoAbstractMonteCarloTrainer {

    private final double discountFactor;

    public AlphaGoFirstVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, AlphaGoTrainablePolicySupplier trainablePolicySupplier, AlphaGoEnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> calculatedVisitedRewards(AlphaGoEpisode episode) {
        Map<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> firstVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistory = episode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                if(!firstVisitSet.containsKey(episodeHistory.get(i).getFirst().getState().getObservation())) {
                    DoubleScalarReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
                    double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
                    firstVisitSet.put(episodeHistory.get(i).getFirst().getState().getObservation(), new ImmutableTuple<>(aggregated, sampledProbabilities));
                }
            }
        }
        return firstVisitSet;
    }
}
