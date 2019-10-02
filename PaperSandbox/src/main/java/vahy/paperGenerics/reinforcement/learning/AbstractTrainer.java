package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.api.predictor.TrainablePredictor;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.paperGenerics.reinforcement.episode.sampler.PaperRolloutGameSampler;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTrainer.class.getName());

    private final double discountFactor;
    private final TrainablePredictor<DoubleVector> trainablePredictor;
    private final PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler;
    protected final RewardAggregator rewardAggregator;

    public AbstractTrainer(PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler,
                           TrainablePredictor<DoubleVector> trainablePredictor,
                           double discountFactor,
                           RewardAggregator rewardAggregator) {
        this.gameSampler = gameSampler;
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.trainablePredictor = trainablePredictor;

    }

    public PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> getGameSampler() {
        return gameSampler;
    }

    protected List<ImmutableTuple<DoubleVector, MutableDataSample>> createDataSample(EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState> paperEpisode) {
        var aggregatedRisk = paperEpisode.isRiskHit() ? 1.0 : 0.0;
        var aggregatedTotalPayoff = 0.0;
        var iterator = paperEpisode.getEpisodeHistory().listIterator(paperEpisode.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDataSample>>();
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = rewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
            if(previous.isPlayerMove()) {
                mutableDataSampleList.add(new ImmutableTuple<>(
                    previous.getFromState().getPlayerObservation(),
                    new MutableDataSample(previous.getPaperPolicyStepRecord().getPolicyProbabilities(), aggregatedTotalPayoff, aggregatedRisk)));
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }

    protected double[] createOutputVector(MutableDataSample dataSample) {
        double[] probabilities = dataSample.getProbabilities();
        double[] outputVector = new double[probabilities.length + PaperModel.POLICY_START_INDEX];
        outputVector[PaperModel.Q_VALUE_INDEX] = dataSample.getReward();
        outputVector[PaperModel.RISK_VALUE_INDEX] = dataSample.getRisk();
        for (int i = 0; i < probabilities.length; i++) {
            outputVector[i + PaperModel.POLICY_START_INDEX] = probabilities[i];
        }
        return outputVector;
    }

    public abstract void trainPolicy(int episodeCount);

    public abstract void printDataset();

    protected double[] evaluatePolicy(DoubleVector doubleVector) {
        return this.trainablePredictor.apply(doubleVector);
    }

    protected void trainPolicy(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        trainablePredictor.train(trainData);
    }

}
