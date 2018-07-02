package vahy.impl.learning.model;

import vahy.api.learning.model.SupervisedTrainableValueModel;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;

import java.util.List;

public class TrainablePolicySupplierImpl<TAction extends Action, TReward extends Reward, TObservation extends Observation> implements TrainablePolicySupplier<TAction, TReward, TObservation> {

    private final SupervisedTrainableValueModel<TReward, TObservation> supervisedTrainableValueModel;

    public TrainablePolicySupplierImpl(SupervisedTrainableValueModel<TReward, TObservation> supervisedTrainableValueModel) {
        this.supervisedTrainableValueModel = supervisedTrainableValueModel;
    }

    @Override
    public SupervisedTrainableValueModel<TReward, TObservation> getTrainableStateValueEvaluator() {
        return supervisedTrainableValueModel;
    }

    @Override
    public Policy<TAction, TReward, TObservation> initializePolicy(State<TAction, TReward, TObservation> initialState) {
        return new Policy<>() {

            @Override
            public double[] getActionProbabilityDistribution(State<TAction, TReward, TObservation> gameState) {
                return new double[0];
            }

            @Override
            public TAction getDiscreteAction(State<TAction, TReward, TObservation> gameState) {
                return null;
            }

            @Override
            public void updateStateOnOpponentActions(List<TAction> opponentActionList) {

            }
        };
    }
}
