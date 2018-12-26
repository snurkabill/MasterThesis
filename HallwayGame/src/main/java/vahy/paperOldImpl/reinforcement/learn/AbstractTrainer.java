package vahy.paperOldImpl.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.StateActionReward;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperOldImpl.policy.EnvironmentPolicySupplier;
import vahy.paperOldImpl.policy.PaperTrainablePaperPolicySupplier;
import vahy.paperOldImpl.reinforcement.episode.PaperRolloutGameSampler;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.paperOldImpl.tree.nodeEvaluator.NodeEvaluator;
import vahy.utils.ImmutableTuple;

import java.util.List;

public abstract class AbstractTrainer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTrainer.class.getName());

    private final PaperRolloutGameSampler gameSampler;
    private final double discountFactor;
    protected final DoubleScalarRewardAggregator rewardAggregator;
    private final PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier;

    public AbstractTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                           PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier,
                           EnvironmentPolicySupplier opponentPolicySupplier,
                           double discountFactor,
                           DoubleScalarRewardAggregator rewardAggregator,
                           int stepCountLimit) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.paperTrainablePolicySupplier = paperTrainablePolicySupplier;
        this.gameSampler = new PaperRolloutGameSampler(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, stepCountLimit);
    }

    public abstract void trainPolicy(int episodeCount);

    public PaperRolloutGameSampler getGameSampler() {
        return gameSampler;
    }

    protected MutableDataSample createDataSample(List<ImmutableTuple<StateActionReward<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>, StepRecord>> episodeHistory, int i) {
        // TODO: very ineffective. Quadratic, could be linear. But so far this is not the bottleneck at all
        DoubleReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
        double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
        double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
        return new MutableDataSample(sampledProbabilities, aggregated, risk);
    }

    protected double[] createOutputVector(MutableDataSample dataSample) {
        double[] probabilities = dataSample.getProbabilities();
        double[] outputVector = new double[probabilities.length + NodeEvaluator.POLICY_START_INDEX];
        outputVector[NodeEvaluator.Q_VALUE_INDEX] = dataSample.getReward().getValue();
        outputVector[NodeEvaluator.RISK_VALUE_INDEX] = dataSample.getRisk();
        for (int i = 0; i < probabilities.length; i++) {
            outputVector[i + NodeEvaluator.POLICY_START_INDEX] = probabilities[i];
        }
        return outputVector;
    }

    protected void trainPolicy(List<ImmutableTuple<DoubleVector, double[]>> trainData) {
        paperTrainablePolicySupplier.train(trainData);
    }

}
