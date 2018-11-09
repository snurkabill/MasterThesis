package vahy.paper.reinforcement.learn;

import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.environment.ActionType;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.paper.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EveryVisitMonteCarloTrainer extends AbstractMonteCarloTrainer {

    public EveryVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier, EnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor, int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator, stepCountLimit);
    }

    @Override
    protected Map<DoubleVectorialObservation, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode) {
        Map<DoubleVectorialObservation, MutableDataSample> everyVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
//            double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i);
                DoubleVectorialObservation experimentalObservation = episodeHistory.get(i).getFirst().getState().getObservation();
                if(!everyVisitSet.containsKey(experimentalObservation)) {
                    everyVisitSet.put(experimentalObservation, dataSample);
                } else {
                    everyVisitSet.get(experimentalObservation).addDataSample(dataSample);
                }
            }
        }
        return everyVisitSet;
    }
}
