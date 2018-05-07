package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.impl.search.node.nodeMetadata.StateValueMetadataImpl;
import vahy.utils.ImmutableTuple;

import java.util.Collection;
import java.util.SplittableRandom;

public class RandomNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends StateValueMetadataImpl<TAction, TReward, Integer, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    extends AbstractPriorityQueueNodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState, Integer> {

    private final SplittableRandom random;

    public RandomNodeSelector(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public void addNode(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> rootNode) {
        queue.add(new ImmutableTuple<>(random.nextInt(), rootNode));
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> rootNodes) {
        rootNodes.forEach(this::addNode);
    }

    @Override
    public void setNewRoot(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> root) {
        // TODO: implement me
    }


}
