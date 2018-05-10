package vahy.environment.agent.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.TraversingTreeUpdater;
import vahy.timer.SimpleTimer;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractTreeSearchPolicy<
    TStateActionMetadata extends StateActionMetadata<DoubleScalarReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<ActionType, DoubleScalarReward, TStateActionMetadata>>
    implements IStatefulPolicy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);

    private final SplittableRandom random;
    private final SearchTreeImpl<ActionType, DoubleScalarReward, DoubleVectorialObservation, TStateActionMetadata, TSearchNodeMetadata, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchTree;
    private final int updateTreeCount;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    public AbstractTreeSearchPolicy(
        SplittableRandom random,
        int updateTreeCount,
        SearchNodeFactory<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            TStateActionMetadata,
            TSearchNodeMetadata,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory,
        Supplier<NodeSelector<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            TStateActionMetadata,
            TSearchNodeMetadata,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> nodeSelectorSupplier,
        Function<
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation,
                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>,
            TStateActionMetadata> stateActionMetadataFactory, NodeTransitionUpdater<ActionType, DoubleScalarReward, TStateActionMetadata, TSearchNodeMetadata> nodeTransitionUpdater,
        ImmutableStateImpl gameState,
        NodeEvaluationSimulator<ActionType, DoubleScalarReward, DoubleVectorialObservation, TStateActionMetadata, TSearchNodeMetadata, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {
        this.random = random;
        this.updateTreeCount = updateTreeCount;
        this.searchTree =
            new SearchTreeImpl<>(
                searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)), null, null),
                nodeSelectorSupplier.get(),
                new BaseNodeExpander<>(searchNodeFactory, stateActionMetadataFactory),
                new TraversingTreeUpdater<>(nodeTransitionUpdater),
                rewardSimulator
            );
    }

    @Override
    public void updateState(ActionType action) {
        this.searchTree.applyAction(action);
    }

    @Override
    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        expandSearchTree(gameState);
        return searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()), random))
            .getKey();
    }

    @Override
    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        throw new UnsupportedOperationException("I will implement this when it will be needed.");
    }

    private void expandSearchTree(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
        if(!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree Policy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
        timer.startTimer();
        for (int i = 0; i < updateTreeCount; i++) {
            logger.debug("Performing tree update for [{}]th iteration", i);
            searchTree.updateTree();
        }
        timer.stopTimer();

        logger.info("Finished updating search tree with total expanded node count: [{}], total created node count: [{}],  max branch factor: [{}], average branch factor [{}] in [{}] seconds, expanded nodes per second: [{}]",
            searchTree.getTotalNodesExpanded(),
            searchTree.getTotalNodesCreated(),
            searchTree.getMaxBranchingFactor(),
            searchTree.calculateAverageBranchingFactor(),
            timer.secondsSpent(),
            timer.samplesPerSec(searchTree.getTotalNodesExpanded()));

        logger.trace("Action estimatedRewards: [{}]", searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .map(x ->  String.valueOf(x.getValue().getEstimatedTotalReward().getValue().doubleValue()))
            .reduce((x, y) -> x + ", " + y));
    }
}
