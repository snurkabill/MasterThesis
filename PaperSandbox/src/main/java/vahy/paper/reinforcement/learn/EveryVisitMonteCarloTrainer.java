package vahy.paper.reinforcement.learn;

import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePolicySupplier;
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

public class EveryVisitMonteCarloTrainer extends AbstractMonteCarloTrainer {

    public EveryVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, PaperTrainablePolicySupplier paperTrainablePolicySupplier, EnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator);
    }

    @Override
    protected Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode) {
        Map<DoubleVectorialObservation, MutableDataSample> everyVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
//            double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i);
                if(!everyVisitSet.containsKey(episodeHistory.get(i).getFirst().getState().getObservation())) {
                    everyVisitSet.put(episodeHistory.get(i).getFirst().getState().getObservation(), dataSample);
                } else {
                    everyVisitSet.get(episodeHistory.get(i).getFirst().getState().getObservation()).addDataSample(dataSample);
                }
            }
        }
        return everyVisitSet;
    }
}
