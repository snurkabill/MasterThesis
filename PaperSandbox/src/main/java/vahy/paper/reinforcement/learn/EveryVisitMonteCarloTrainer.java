package vahy.paper.reinforcement.learn;

import vahy.api.model.StateActionReward;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EveryVisitMonteCarloTrainer extends AbstractMonteCarloTrainer {

    public EveryVisitMonteCarloTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier, PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier, EnvironmentPolicySupplier opponentPolicySupplier, DoubleScalarRewardAggregator rewardAggregator, double discountFactor, int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator, stepCountLimit);
    }

    @Override
    protected Map<DoubleVector, MutableDataSample> calculatedVisitedRewards(PaperEpisode paperEpisode) {
        Map<DoubleVector, MutableDataSample> everyVisitSet = new LinkedHashMap<>();
        List<ImmutableTuple<StateActionReward<HallwayAction, DoubleReward, DoubleVector, HallwayStateImpl>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
//            double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i);
                DoubleVector experimentalObservation = episodeHistory.get(i).getFirst().getState().getObservation();
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
