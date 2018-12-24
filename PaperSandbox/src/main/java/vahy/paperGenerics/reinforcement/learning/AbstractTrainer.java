package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.PaperRolloutGameSampler;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;

import java.util.List;

public abstract class AbstractTrainer<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends PaperMetadata<TAction, DoubleReward>,
    TState extends PaperState<TAction, DoubleReward, DoubleVector, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTrainer.class.getName());

    private final double discountFactor;
    private final TrainableNodeEvaluator<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperNodeEvaluator;
    private final PaperRolloutGameSampler<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> gameSampler;
    protected final RewardAggregator<DoubleReward> rewardAggregator;

    public AbstractTrainer(InitialStateSupplier<TAction, DoubleReward, DoubleVector, TState> initialStateSupplier,
                           TrainablePaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                           PaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> opponentPolicySupplier,
                           TrainableNodeEvaluator<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperNodeEvaluator,
                           double discountFactor,
                           RewardAggregator<DoubleReward> rewardAggregator,
                           int stepCountLimit) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.paperNodeEvaluator = paperNodeEvaluator;
        this.gameSampler = new PaperRolloutGameSampler<>(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, stepCountLimit);
    }

    public PaperRolloutGameSampler<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> getGameSampler() {
        return gameSampler;
    }

    protected MutableDataSample createDataSample(List<ImmutableTuple<StateActionReward<TAction, DoubleReward, DoubleVector, TState>, StepRecord<DoubleReward>>> episodeHistory,
                                                 int i) {
        // TODO: very ineffective. Quadratic, could be linear. But so far this is not the bottleneck at all
        DoubleReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
        double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
        double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getState().isRiskHit() ? 1.0 : 0.0;
        return new MutableDataSample(sampledProbabilities, aggregated, risk);
    }

    protected double[] createOutputVector(MutableDataSample dataSample) {
        double[] probabilities = dataSample.getProbabilities();
        double[] outputVector = new double[probabilities.length + PaperModel.POLICY_START_INDEX];
        outputVector[PaperModel.Q_VALUE_INDEX] = dataSample.getReward().getValue();
        outputVector[PaperModel.RISK_VALUE_INDEX] = dataSample.getRisk();
        for (int i = 0; i < probabilities.length; i++) {
            outputVector[i + PaperModel.POLICY_START_INDEX] = probabilities[i];
        }
        return outputVector;
    }

    public abstract void trainPolicy(int episodeCount);

    protected void trainPolicy(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        paperNodeEvaluator.train(trainData);
    }

}
