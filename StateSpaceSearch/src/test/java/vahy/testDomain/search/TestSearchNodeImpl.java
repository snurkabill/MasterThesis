package vahy.testDomain.search;

import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.Map;

public class TestSearchNodeImpl<TSearchNodeMetadata extends SearchNodeMetadata<DoubleReward>>
    extends SearchNodeImpl<TestAction, DoubleReward, DoubleVector, TestState, TSearchNodeMetadata, TestState> {

    public TestSearchNodeImpl(TestState wrappedState,
                              TSearchNodeMetadata searchNodeMetadata,
                              Map<TestAction, SearchNode<TestAction, DoubleReward, DoubleVector, TestState, TSearchNodeMetadata, TestState>> childNodeMap) {
        super(wrappedState, searchNodeMetadata, childNodeMap);
    }

    public TestSearchNodeImpl(TestState wrappedState,
                              TSearchNodeMetadata searchNodeMetadata,
                              Map<TestAction, SearchNode<TestAction, DoubleReward, DoubleVector, TestState, TSearchNodeMetadata, TestState>> childNodeMap,
                              SearchNode<TestAction, DoubleReward, DoubleVector, TestState, TSearchNodeMetadata, TestState> parent,
                              TestAction appliedAction) {
        super(wrappedState, searchNodeMetadata, childNodeMap, parent, appliedAction);
    }
}
