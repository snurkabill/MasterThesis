package vahy.environment.agent.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.simulation.CumulativeRewardSimulator;
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
    implements IOneHotPolicy {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);

    private final SplittableRandom random;
    private final SearchNodeFactory<
        ActionType,
        DoubleScalarReward,
        DoubleVectorialObservation,
        TStateActionMetadata,
        TSearchNodeMetadata,
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory;
    private final Supplier<NodeSelector<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            TStateActionMetadata,
            TSearchNodeMetadata,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> nodeSelectorSupplier;
    private final Function<StateRewardReturn<
                ActionType,
                DoubleScalarReward,
                DoubleVectorialObservation,
                State<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation>>,
                TStateActionMetadata> stateActionMetadataFactory;
    private final NodeTransitionUpdater<ActionType, DoubleScalarReward, TStateActionMetadata, TSearchNodeMetadata> nodeTransitionUpdater;
    private final int uprateTreeCount;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    public AbstractTreeSearchPolicy(
        SplittableRandom random,
        int uprateTreeCount,
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
            TStateActionMetadata> stateActionMetadataFactory, NodeTransitionUpdater<ActionType, DoubleScalarReward, TStateActionMetadata, TSearchNodeMetadata> nodeTransitionUpdater) {
        this.random = random;
        this.searchNodeFactory = searchNodeFactory;
        this.nodeSelectorSupplier = nodeSelectorSupplier;
        this.uprateTreeCount = uprateTreeCount;
        this.stateActionMetadataFactory = stateActionMetadataFactory;
        this.nodeTransitionUpdater = nodeTransitionUpdater;
    }

    @Override
    public ActionType getDiscreteAction(ImmutableStateImpl gameState) {
        SearchTreeImpl<ActionType, DoubleScalarReward, DoubleVectorialObservation, TStateActionMetadata, TSearchNodeMetadata, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchTree =
            new SearchTreeImpl<>(
            searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(gameState, new DoubleScalarReward(0.0)), null, null),
            nodeSelectorSupplier.get(),
            new BaseNodeExpander<>(searchNodeFactory, stateActionMetadataFactory),
            new TraversingTreeUpdater<>(nodeTransitionUpdater),
            new CumulativeRewardSimulator<>()
        );

        timer.startTimer();
        for (int i = 0; i < uprateTreeCount; i++) {
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

        ActionType action = searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()), random))
            .getKey();

        return action;
    }

}
