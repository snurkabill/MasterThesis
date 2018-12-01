package vahy.paperGenerics;

import vahy.api.model.Action;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.RandomDistributionUtils;
import vahy.utils.ReflectionHacks;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

public class PaperPolicyImplWithExploration<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleScalarReward,
    TObservation extends DoubleVectorialObservation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>>
    implements PaperPolicy<TAction, TReward, TObservation, TState> {

    private final SplittableRandom random;
    private final PaperPolicyImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> innerPolicy;
    private final double explorationConstant;
    private final double temperature;
    private final TAction[] playerActions;

    public PaperPolicyImplWithExploration(Class<TAction> clazz,
                                          SplittableRandom random,
                                          PaperPolicyImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> innerPolicy,
                                          double explorationConstant,
                                          double temperature) {
        this.random = random;
        this.innerPolicy = innerPolicy;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        TAction[] allActions = clazz.getEnumConstants();
        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).toArray(size -> ReflectionHacks.arrayFromGenericClass(clazz, size));
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(TState gameState) {
        return innerPolicy.getPriorActionProbabilityDistribution(gameState);
    }

    @Override
    public DoubleScalarReward getEstimatedReward(TState gameState) {
        return innerPolicy.getEstimatedReward(gameState);
    }

    @Override
    public double getEstimatedRisk(TState gameState) {
        return innerPolicy.getEstimatedRisk(gameState);
    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        return innerPolicy.getActionProbabilityDistribution(gameState);
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {
        TAction discreteAction = innerPolicy.getDiscreteAction(gameState);
        double randomDouble = random.nextDouble();
        if(randomDouble > explorationConstant) {
            return discreteAction;
        } else {
            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
            double[] exponentiation = new double[actionProbabilityDistribution.length];
            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                exponentiation[i] = Math.exp(actionProbabilityDistribution[i] / temperature);
            }
            double sum = Arrays.stream(exponentiation).sum();
            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                exponentiation[i] = exponentiation[i] / sum;
            }
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(exponentiation, random);
            return playerActions[index];
        }
    }

    @Override
    public void updateStateOnOpponentActions(List<TAction> opponentActionList) {
        innerPolicy.updateStateOnOpponentActions(opponentActionList);
    }
}
