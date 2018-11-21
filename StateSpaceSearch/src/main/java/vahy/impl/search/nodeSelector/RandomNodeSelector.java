package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;

import java.util.SplittableRandom;

public class RandomNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final SplittableRandom random;

    public RandomNodeSelector(SplittableRandom random) {
        this.random = random;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node) {
        TAction[] allPossibleActions = node.getAllPossibleActions();
        return allPossibleActions[random.nextInt(allPossibleActions.length)];
    }
}
