package vahy.AlphaGo.reinforcement.learn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoRolloutGameSampler;
import vahy.AlphaGo.reinforcement.episode.AlphaGoStepRecord;
import vahy.AlphaGo.tree.AlphaGoNodeEvaluator;
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

    private final AlphaGoRolloutGameSampler gameSampler;
    private final double discountFactor;
    protected final DoubleScalarRewardAggregator rewardAggregator;
    private final AlphaGoTrainablePolicySupplier trainablePolicySupplier;

    public AbstractTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                           AlphaGoTrainablePolicySupplier trainablePolicySupplier,
                           AlphaGoEnvironmentPolicySupplier opponentPolicySupplier,
                           double discountFactor,
                           DoubleScalarRewardAggregator rewardAggregator) {
        this.discountFactor = discountFactor;
        this.rewardAggregator = rewardAggregator;
        this.trainablePolicySupplier = trainablePolicySupplier;
        this.gameSampler = new AlphaGoRolloutGameSampler(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier);
    }

    public abstract void trainPolicy(int episodeCount);

    public AlphaGoRolloutGameSampler getGameSampler() {
        return gameSampler;
    }

    protected MutableDataSample createDataSample(List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistory, int i) {
        // TODO: very ineffective. Quadratic, could be linear. But so far this is not the bottleneck at all
        DoubleScalarReward aggregated = rewardAggregator.aggregateDiscount(episodeHistory.stream().skip(i).map(x -> x.getFirst().getReward()), discountFactor);
        double[] sampledProbabilities = episodeHistory.get(i).getSecond().getPolicyProbabilities();
        double risk = episodeHistory.get(episodeHistory.size() - 1).getFirst().getAction().isTrap() ? 1.0 : 0.0;
        return new MutableDataSample(sampledProbabilities, aggregated, risk);
    }

    protected double[] createOutputVector(MutableDataSample dataSample) {
        double[] probabilities = dataSample.getProbabilities();
        double[] outputVector = new double[probabilities.length + AlphaGoNodeEvaluator.POLICY_START_INDEX];
        outputVector[AlphaGoNodeEvaluator.Q_VALUE_INDEX] = dataSample.getReward().getValue();
        outputVector[AlphaGoNodeEvaluator.RISK_VALUE_INDEX] = dataSample.getRisk();
        for (int i = 0; i < probabilities.length; i++) {
            outputVector[i + AlphaGoNodeEvaluator.POLICY_START_INDEX] = probabilities[i];
        }
        return outputVector;
    }

    protected void trainPolicy(List<ImmutableTuple<DoubleVectorialObservation, double[]>> trainData) {
        trainablePolicySupplier.train(trainData);
    }

}
