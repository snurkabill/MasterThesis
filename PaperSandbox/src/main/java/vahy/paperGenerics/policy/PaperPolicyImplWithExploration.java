package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class PaperPolicyImplWithExploration<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>>
    implements PaperPolicy<TAction, TReward, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicyImplWithExploration.class.getName());

    private final SplittableRandom random;
    private final PaperPolicy<TAction, TReward, TObservation, TState> innerPolicy;
    private final double explorationConstant;
    private final double temperature;
    private final List<TAction> playerActions;
    private final boolean[] actionMask;

    public PaperPolicyImplWithExploration(Class<TAction> clazz,
                                          SplittableRandom random,
                                          PaperPolicy<TAction, TReward, TObservation, TState> innerPolicy,
                                          double explorationConstant,
                                          double temperature) {
        this.random = random;
        this.innerPolicy = innerPolicy;
        this.explorationConstant = explorationConstant;
        this.temperature = temperature;
        TAction[] allActions = clazz.getEnumConstants();
        this.playerActions = Arrays.stream(allActions).filter(Action::isPlayerAction).collect(Collectors.toCollection(ArrayList::new));
        this.actionMask = new boolean[playerActions.size()];
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(TState gameState) {
        return innerPolicy.getPriorActionProbabilityDistribution(gameState);
    }

    @Override
    public TReward getEstimatedReward(TState gameState) {
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
            logger.debug("Exploitation action [{}].", discreteAction);
            return discreteAction;
        } else {
            updateMask(gameState.getAllPossibleActions());

            double[] actionProbabilityDistribution = this.getActionProbabilityDistribution(gameState);
            double[] exponentiation = new double[actionProbabilityDistribution.length];
            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                if(actionMask[i]) {
                    exponentiation[i] = Math.exp(actionProbabilityDistribution[i] / temperature);
                }
            }
            double sum = Arrays.stream(exponentiation).sum();
            for (int i = 0; i < actionProbabilityDistribution.length; i++) {
                exponentiation[i] = exponentiation[i] / sum;
            }
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(exponentiation, random);
            logger.debug("Exploration action [{}]", playerActions.get(index));
            return playerActions.get(index);
        }
    }

    private void updateMask(TAction[] possibleActions) {
        for (int i = 0; i < playerActions.size(); i++) {
            this.actionMask[i] = false;
            for (TAction possibleAction : possibleActions) {
                if(playerActions.get(i) == possibleAction) {
                    this.actionMask[i] = true;
                    break;
                }
            }
        }
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        innerPolicy.updateStateOnPlayedActions(opponentActionList);
    }
}
