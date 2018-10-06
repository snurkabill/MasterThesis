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

public class AlphaGoEveryVisitMonteCarloTrainer extends AlphaGoAbstractMonteCarloTrainer {

    private final double discountFactor;

    public AlphaGoEveryVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, AlphaGoTrainablePolicySupplier trainablePolicySupplier, AlphaGoEnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(AlphaGoEpisode episode) {
        Map<DoubleVectorialObservation, MutableDataSample> everyVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistory = episode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                DoubleScalarReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
                double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
                if(!everyVisitSet.containsKey(episodeHistory.get(i).getFirst().getState().getObservation())) {
                    everyVisitSet.put(episodeHistory.get(i).getFirst().getState().getObservation(), new MutableDataSample(sampledProbabilities, aggregated, risk));
                } else {
                    everyVisitSet.get(episodeHistory.get(i).getFirst().getState().getObservation()).addDataSample(sampledProbabilities, aggregated.getValue(), risk);
                }
            }
        }
        return everyVisitSet;
    }
}
