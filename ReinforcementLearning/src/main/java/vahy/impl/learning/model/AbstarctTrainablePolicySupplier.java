package vahy.impl.learning.model;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;

public abstract class AbstarctTrainablePolicySupplier<TAction extends Action, TReward extends Reward, TObservation extends Observation> implements TrainablePolicySupplier<TAction, TReward, TObservation> {

    private final SupervisedTrainableModel supervisedTrainableModel;

    public AbstarctTrainablePolicySupplier(SupervisedTrainableModel supervisedTrainableModel) {
        this.supervisedTrainableModel = supervisedTrainableModel;
    }

    @Override
    public SupervisedTrainableModel getTrainableStateEvaluator() {
        return supervisedTrainableModel;
    }

}
