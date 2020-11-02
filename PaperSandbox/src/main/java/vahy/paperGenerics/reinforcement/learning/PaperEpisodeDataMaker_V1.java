package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

public class PaperEpisodeDataMaker_V1<TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>> extends AbstractPaperEpisodeDataMaker<TAction, TState> {

    public PaperEpisodeDataMaker_V1(int playerPolicyId, int actionCount, double discountFactor, DataAggregator dataAggregator) {
        super(playerPolicyId, actionCount, discountFactor, dataAggregator);
    }

    public PaperEpisodeDataMaker_V1(int playerPolicyId, int actionCount, double discountFactor, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(playerPolicyId, actionCount, discountFactor, policyObservationLookbackSize, dataAggregator);
    }

    @Override
    protected ImmutableTuple<DoubleVector, MutableDoubleArray> createDatasample(double[] aggregatedRisk,
                                                                                double[] aggregatedTotalPayoff,
                                                                                ImmutableTuple<EpisodeStepRecord<TAction, DoubleVector, TState>, StateWrapper<TAction, DoubleVector, TState>> previous) {
        if (entityCount != 2) {
            throw new IllegalStateException("Class [" + PaperEpisodeDataMaker_V1.class + "] must be used for exactly 2 game entities. Provided: [ " + entityCount + "]");
        }

        var doubleArray = new double[entityCount * 2 + actionCount];
        if (previous.getFirst().getFromState().isEnvironmentEntityOnTurn()) {
            var action = previous.getFirst().getAction();
            var actionId = action.ordinal();
            doubleArray[entityCount * 2 + actionId] = 1.0;
            System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
            System.arraycopy(aggregatedRisk, 0, doubleArray, entityCount, aggregatedTotalPayoff.length);
            return new ImmutableTuple<>(previous.getSecond().getObservation(), new MutableDoubleArray(doubleArray, false));
        } else {
            if (previous.getFirst().getPolicyIdOnTurn() == playerPolicyId) {
                var policyArray = previous.getFirst().getPolicyStepRecord().getPolicyProbabilities();
                System.arraycopy(policyArray, 0, doubleArray, entityCount * 2, policyArray.length);
                System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                System.arraycopy(aggregatedRisk, 0, doubleArray, entityCount, aggregatedTotalPayoff.length);
                return new ImmutableTuple<>(previous.getSecond().getObservation(), new MutableDoubleArray(doubleArray, false));
            } else {
                throw new IllegalStateException("Class [" + PaperEpisodeDataMaker_V1.class + "] can't be used for multiplayer");
            }
        }
    }

}
