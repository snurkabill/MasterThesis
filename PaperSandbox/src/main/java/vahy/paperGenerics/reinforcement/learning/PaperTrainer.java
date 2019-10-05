package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.GameSampler;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.learning.trainer.AbstractTrainer;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaperTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecordBase>
    extends AbstractTrainer<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PaperTrainer.class.getName());

    private final double discountFactor;

    public PaperTrainer(GameSampler<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> gameSampler,
                        TrainablePredictor trainablePredictor,
                        double discountFactor,
                        DataAggregator dataAggregator) {
        super(trainablePredictor, gameSampler, dataAggregator);
        this.discountFactor = discountFactor;
    }

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(
        EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> paperEpisode) {
        var episodeHistory = paperEpisode.getEpisodeHistory();
        var aggregatedRisk = episodeHistory.get(episodeHistory.size() - 1).getToState().isRiskHit() ? 1.0 : 0.0;
        var aggregatedTotalPayoff = 0.0;
        var iterator = episodeHistory.listIterator(paperEpisode.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
            if(previous.isPlayerMove()) {
                var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
                var doubleArray = new double[policyArray.length + PaperModel.POLICY_START_INDEX];
                doubleArray[PaperModel.Q_VALUE_INDEX] = aggregatedTotalPayoff;
                doubleArray[PaperModel.RISK_VALUE_INDEX] = aggregatedRisk;
                System.arraycopy(policyArray, 0, doubleArray, PaperModel.POLICY_START_INDEX, policyArray.length);
                mutableDataSampleList.add(new ImmutableTuple<>(
                    previous.getFromState().getPlayerObservation(),
                    new MutableDoubleArray(doubleArray, false)));
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }

//    protected double[] createOutputVector(MutableDataSample dataSample) {
//        double[] probabilities = dataSample.getProbabilities();
//        double[] outputVector = new double[probabilities.length + PaperModel.POLICY_START_INDEX];
//        outputVector[PaperModel.Q_VALUE_INDEX] = dataSample.getReward();
//        outputVector[PaperModel.RISK_VALUE_INDEX] = dataSample.getRisk();
//        for (int i = 0; i < probabilities.length; i++) {
//            outputVector[i + PaperModel.POLICY_START_INDEX] = probabilities[i];
//        }
//        return outputVector;
//    }

}
