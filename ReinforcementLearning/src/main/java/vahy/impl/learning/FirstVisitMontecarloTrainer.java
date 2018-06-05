package vahy.impl.learning;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.SupervisedTrainableStateValueModel;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.policy.PolicySupplier;
import vahy.utils.MutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FirstVisitMontecarloTrainer<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    private final MonteCarloGameSampler<TAction, TReward, TObservation> monteCarloGameSampler;
    private final SupervisedTrainableStateValueModel<TReward, TObservation> trainableStateValueModel;
    private final Map<TObservation, MutableTuple<Integer, TReward>> firstVisitAverageRewardMap = new LinkedHashMap<>();

    public FirstVisitMontecarloTrainer(InitialStateSupplier<TAction, TReward, TObservation> initialStateSupplier,
                                       TrainablePolicySupplier<TAction, TReward, TObservation> trainablePolicySupplier,
                                       PolicySupplier<TAction, TReward, TObservation> opponentPolicySupplier) {
        monteCarloGameSampler = new MonteCarloGameSampler<>(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier);
        this.trainableStateValueModel = trainablePolicySupplier.getTrainableStateValueEvaluator();
    }

    public void trainPolicy() {
        List<List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>>> episodeHistoryList = monteCarloGameSampler.sampleEpisodes(1);
        addFirstVisitRewards(episodeHistoryList);

        // trainableStateValueModel.fit();
    }

    private void addFirstVisitRewards(List<List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>>> episodeHistoryList) {
        for (List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> episode : episodeHistoryList) {
            for (StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> episodeStep : episode) {
                // TODO: finish me
            }
        }
    }


}
