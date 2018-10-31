package vahy.paper.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperRolloutGameSampler;
import vahy.paper.reinforcement.episode.StepRecord;
import vahy.paper.tree.nodeEvaluator.NodeEvaluator;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.environment.ActionType;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
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
                           DoubleScalarRewardAggregator rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.paperTrainablePolicySupplier = paperTrainablePolicySupplier;
        this.gameSampler = new PaperRolloutGameSampler(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier);
    }

    public abstract void trainPolicy(int episodeCount);

    public PaperRolloutGameSampler getGameSampler() {
        return gameSampler;
    }

    protected MutableDataSample createDataSample(List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, StepRecord>> episodeHistory, int i) {
        // TODO: very ineffective. Quadratic, could be linear. But so far this is not the bottleneck at all
        DoubleScalarReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
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

    protected void trainPolicy(List<ImmutableTuple<DoubleVectorialObservation, double[]>> trainData) {
        paperTrainablePolicySupplier.train(trainData);
    }

}
