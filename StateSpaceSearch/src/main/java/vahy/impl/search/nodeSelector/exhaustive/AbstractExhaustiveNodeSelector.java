package vahy.impl.search.nodeSelector.exhaustive;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractExhaustiveNodeSelector<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    implements NodeSelector<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    protected final LinkedList<SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> nodeQueue;

    public AbstractExhaustiveNodeSelector() {
        this.nodeQueue = new LinkedList<>(); // using linked implementation here // TODO: generalize
    }

    @Override
    public void addNodes(Collection<SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> rootNodes) {
        for (SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> rootNode : rootNodes) {
            this.addNode(rootNode);
        }
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> selectNextNode() {
        return nodeQueue.removeFirst();
    }

}
