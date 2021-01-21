package vahy.ralph.reinforcement.learning;

import vahy.RiskState;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeStepRecordWithObservation;
import vahy.api.model.Action;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

public class RalphEpisodeDataMaker_V1<TAction extends Enum<TAction> & Action, TState extends RiskState<TAction, DoubleVector, TState>> extends AbstractRalphEpisodeDataMaker<TAction, TState> {

    public RalphEpisodeDataMaker_V1(int playerPolicyId, int actionCount, double discountFactor, DataAggregator dataAggregator) {
        super(playerPolicyId, actionCount, discountFactor, dataAggregator);
    }

    public RalphEpisodeDataMaker_V1(int playerPolicyId, int actionCount, double discountFactor, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(playerPolicyId, actionCount, discountFactor, policyObservationLookbackSize, dataAggregator);
    }

    @Override
    protected ImmutableTuple<DoubleVector, MutableDoubleArray> createDatasample(double[] aggregatedRisk,
                                                                                double[] aggregatedTotalPayoff,
                                                                                EpisodeStepRecordWithObservation<TAction, TState> previous) {
        if (entityCount != 2) {
            throw new IllegalStateException("Class [" + RalphEpisodeDataMaker_V1.class + "] must be used for exactly 2 game entities. Provided: [ " + entityCount + "]");
        }
        var doubleArray = new double[entityCount * 2 + actionCount];
        var observation = previous.getObservation();
        var step = previous.getEpisodeStepRecord();
        if (step.getFromState().isEnvironmentEntityOnTurn()) {
            var action = step.getAction();
            var actionId = action.ordinal();
            doubleArray[entityCount * 2 + actionId] = 1.0;
            System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
            System.arraycopy(aggregatedRisk, 0, doubleArray, entityCount, aggregatedTotalPayoff.length);
            return new ImmutableTuple<>(observation, new MutableDoubleArray(doubleArray, false));
        } else {
            if (step.getPolicyIdOnTurn() == playerPolicyId) {
                var policyArray = step.getPolicyStepRecord().getPolicyProbabilities();
                System.arraycopy(policyArray, 0, doubleArray, entityCount * 2, policyArray.length);
                System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                System.arraycopy(aggregatedRisk, 0, doubleArray, entityCount, aggregatedTotalPayoff.length);
                return new ImmutableTuple<>(observation, new MutableDoubleArray(doubleArray, false));
            } else {
                throw new IllegalStateException("Class [" + RalphEpisodeDataMaker_V1.class + "] can't be used for multiplayer");
            }
        }
    }

}
