package vahy.testDomain.search;

import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.Map;

public class TestSearchNodeImpl<TSearchNodeMetadata extends SearchNodeMetadata<DoubleScalarReward>>
    extends SearchNodeImpl<TestAction, DoubleScalarReward, DoubleVectorialObservation, TSearchNodeMetadata, TestState> {

    public TestSearchNodeImpl(TestState wrappedState,
                              TSearchNodeMetadata searchNodeMetadata,
                              Map<TestAction, SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, TSearchNodeMetadata, TestState>> childNodeMap) {
        super(wrappedState, searchNodeMetadata, childNodeMap);
    }

    public TestSearchNodeImpl(TestState wrappedState,
                              TSearchNodeMetadata searchNodeMetadata,
                              Map<TestAction, SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, TSearchNodeMetadata, TestState>> childNodeMap,
                              SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, TSearchNodeMetadata, TestState> parent,
                              TestAction appliedAction) {
        super(wrappedState, searchNodeMetadata, childNodeMap, parent, appliedAction);
    }
}
