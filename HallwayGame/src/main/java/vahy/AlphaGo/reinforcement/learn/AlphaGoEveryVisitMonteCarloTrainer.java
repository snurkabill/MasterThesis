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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlphaGoEveryVisitMonteCarloTrainer extends AlphaGoAbstractMonteCarloTrainer {

    private final double discountFactor;

    public AlphaGoEveryVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, AlphaGoTrainablePolicySupplier trainablePolicySupplier, AlphaGoEnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, rewardAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected Map<DoubleVectorialObservation, ImmutableTuple<DoubleScalarReward, double[]>> calculatedVisitedRewards(AlphaGoEpisode episode) {
        Map<DoubleVectorialObservation, List<ImmutableTuple<DoubleScalarReward, double[]>>> everyVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistory = episode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                DoubleScalarReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
                double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
                if(!everyVisitSet.containsKey(episodeHistory.get(i).getFirst().getState().getObservation())) {
                    everyVisitSet.put(episodeHistory.get(i).getFirst().getState().getObservation(), Collections.singletonList(new ImmutableTuple<>(aggregated, sampledProbabilities)));
                } else {
                    List<ImmutableTuple<DoubleScalarReward, double[]>> sampledTarget = everyVisitSet.get(episodeHistory.get(i).getFirst().getState().getObservation());
                    sampledTarget.add(new ImmutableTuple<>(aggregated, sampledProbabilities));
                }
            }
        }
        return everyVisitSet.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            x ->
            {
                List<ImmutableTuple<DoubleScalarReward, double[]>> sampledTargets = x.getValue();
                DoubleScalarReward averagedReward = rewardAggregator.averageReward(sampledTargets.stream().map(ImmutableTuple::getFirst));
                double[] averagedProbabilities = new double[sampledTargets.get(0).getSecond().length];
                for (ImmutableTuple<DoubleScalarReward, double[]> sampledTarget : sampledTargets) {
                    double[] nextVector = sampledTarget.getSecond();
                    for (int j = 0; j < averagedProbabilities.length; j++) {
                        averagedProbabilities[j] += nextVector[j];
                    }
                }
                for (int i = 0; i < averagedProbabilities.length; i++) {
                    averagedProbabilities[i] /= sampledTargets.size();
                }
                return new ImmutableTuple<>(averagedReward, averagedProbabilities);
            })
        );
    }
}
