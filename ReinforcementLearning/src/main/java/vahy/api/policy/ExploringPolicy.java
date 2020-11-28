package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

import java.util.SplittableRandom;

public abstract class ExploringPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>>
    extends RandomizedPolicy<TAction, TObservation, TState> {

    private final boolean isExplorationDisabled;
    private final double explorationConstant;

    protected PlayingDistribution<TAction> playingDistribution = null;

    protected ExploringPolicy(SplittableRandom random, int policyId, double explorationConstant) {
        super(random, policyId);
        this.isExplorationDisabled = explorationConstant == 0.0;
        this.explorationConstant = explorationConstant;
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        boolean exploitation = isExplorationDisabled || random.nextDouble() > explorationConstant;
        playingDistribution = exploitation ? inferenceBranch(gameState) : explorationBranch(gameState);
        if(DEBUG_ENABLED) {
            if(exploitation) {
                logger.debug("Exploitation action: [{}]", playingDistribution.toString());
            } else {
                logger.debug("Exploration action: [{}]", playingDistribution.toString());
            }
        }
        return playingDistribution.getPlayedAction();
    }

//    @Override
//    public TPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
//        return new PolicyRecordBase(playingDistribution.getDistribution(), playingDistribution.getPredictedReward());
//    }

    protected abstract PlayingDistribution<TAction> inferenceBranch(StateWrapper<TAction, TObservation, TState> gameState);

    protected abstract PlayingDistribution<TAction> explorationBranch(StateWrapper<TAction, TObservation, TState> gameState);
}
