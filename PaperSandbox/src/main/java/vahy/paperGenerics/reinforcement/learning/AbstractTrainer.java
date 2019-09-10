package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.paperGenerics.reinforcement.episode.PaperRolloutGameSampler;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTrackerSettings;

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
    private final TrainableNodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperNodeEvaluator;
    private final PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler;
    protected final RewardAggregator rewardAggregator;

    public AbstractTrainer(InitialStateSupplier<TAction, DoubleVector, TOpponentObservation, TState> initialStateSupplier,
                           TrainablePaperPolicySupplier<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                           PaperPolicySupplier<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                           TrainableNodeEvaluator<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperNodeEvaluator,
                           double discountFactor,
                           RewardAggregator rewardAggregator,
                           ProgressTrackerSettings progressTrackerSettings,
                           int stepCountLimit,
                           int threadCount) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.paperNodeEvaluator = paperNodeEvaluator;
        this.gameSampler = new PaperRolloutGameSampler<>(
            initialStateSupplier,
            paperTrainablePolicySupplier,
            opponentPolicySupplier,
            progressTrackerSettings,
            stepCountLimit,
            threadCount);
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
                    new MutableDataSample(previous.getPolicyStepRecord().getPolicyProbabilities(), aggregatedTotalPayoff, aggregatedRisk)));
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
        return this.paperNodeEvaluator.evaluate(doubleVector);
    }

    protected void trainPolicy(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        paperNodeEvaluator.train(trainData);
    }

}
