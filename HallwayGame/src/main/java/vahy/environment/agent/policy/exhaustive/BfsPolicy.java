package vahy.environment.agent.policy.exhaustive;

import vahy.api.model.State;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.nodeSelector.exhaustive.BfsNodeSelector;
import vahy.impl.search.simulation.CumulativeRewardSimulator;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.ArgmaxDiscountEstimatedRewardTransitionUpdater;
import vahy.impl.search.update.TraversingTreeUpdater;

import java.util.Comparator;
import java.util.LinkedHashMap;

public class BfsPolicy implements IOneHotPolicy {

    private final SearchNodeFactory<
        ActionType,
        DoubleScalarReward,
        DoubleVectorialObservation,
        EmptyStateActionMetadata<DoubleScalarReward>,
        EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory;

    public BfsPolicy() {
        this.searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
            (stateRewardReturn, parent) -> {
                Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                return new EmptySearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
            }
        );
    }

    @Override
    public ActionType getDiscreteAction(ImmutableStateImpl gameState) {
        SearchTreeImpl<ActionType, DoubleScalarReward, DoubleVectorialObservation, EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchTree = new SearchTreeImpl<>(
                searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)), null, null),
                new BfsNodeSelector<>(),
                new BaseNodeExpander<>(searchNodeFactory, stateRewardReturn -> new EmptyStateActionMetadata<>(stateRewardReturn.getReward())),
                new TraversingTreeUpdater<>(new ArgmaxDiscountEstimatedRewardTransitionUpdater<>(0.9)),
                new CumulativeRewardSimulator<>()
            );

        for (int i = 0; i < 100; i++) {
            //System.out.println("update " + i);
            searchTree.updateTree();
        }

        return searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .max(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()))
            .get()
            .getKey();
    }
}
