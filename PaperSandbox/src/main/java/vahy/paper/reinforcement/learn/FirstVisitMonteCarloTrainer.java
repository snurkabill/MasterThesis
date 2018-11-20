package vahy.paper.reinforcement.learn;

import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.paper.reinforcement.episode.StepRecord;
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

public class FirstVisitMonteCarloTrainer extends AbstractMonteCarloTrainer {

    public FirstVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier, EnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor, int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator, stepCountLimit);
    }

    @Override
    protected Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode) {
        Map<DoubleVectorialObservation, MutableDataSample> firstVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                if(!firstVisitSet.containsKey(episodeHistory.get(i).getFirst().getState().getObservation())) {
                    firstVisitSet.put(episodeHistory.get(i).getFirst().getState().getObservation(), createDataSample(episodeHistory, i));
                }
            }
        }
        return firstVisitSet;
    }


}
