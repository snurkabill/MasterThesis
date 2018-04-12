package vahy.environment.agent.policy.exhaustive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import vahy.impl.search.nodeSelector.exhaustive.AbstractExhaustiveNodeSelector;
import vahy.impl.search.simulation.CumulativeRewardSimulator;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.ArgmaxDiscountEstimatedRewardTransitionUpdater;
import vahy.impl.search.update.TraversingTreeUpdater;
import vahy.timer.SimpleTimer;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public abstract class ExhaustivePolicy implements IOneHotPolicy {

    private static final Logger logger = LoggerFactory.getLogger(ExhaustivePolicy.class);

    private final SplittableRandom random;
    private final double discountFactor;
    private final SearchNodeFactory<
        ActionType,
        DoubleScalarReward,
        DoubleVectorialObservation,
        EmptyStateActionMetadata<DoubleScalarReward>,
        EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory;
    private final Supplier<AbstractExhaustiveNodeSelector<
                ActionType,
                DoubleScalarReward,
                DoubleVectorialObservation,
                EmptyStateActionMetadata<DoubleScalarReward>,
                EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
                State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> nodeSelectorSupplier;
    private final int uprateTreeCount;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    public ExhaustivePolicy(
        SplittableRandom random,
        double discountFactor,
        int uprateTreeCount,
        Supplier<AbstractExhaustiveNodeSelector<
            ActionType,
            DoubleScalarReward,
            DoubleVectorialObservation,
            EmptyStateActionMetadata<DoubleScalarReward>,
            EmptySearchNodeMetadata<ActionType, DoubleScalarReward>,
            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> nodeSelectorSupplier) {
        this.random = random;
        this.discountFactor = discountFactor;
        this.nodeSelectorSupplier = nodeSelectorSupplier;
        this.uprateTreeCount = uprateTreeCount;
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
                nodeSelectorSupplier.get(),
                new BaseNodeExpander<>(searchNodeFactory, stateRewardReturn -> new EmptyStateActionMetadata<>(stateRewardReturn.getReward())),
                new TraversingTreeUpdater<>(new ArgmaxDiscountEstimatedRewardTransitionUpdater<>(discountFactor)),
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

        return searchTree
            .getRoot()
            .getSearchNodeMetadata()
            .getStateActionMetadataMap()
            .entrySet()
            .stream()
            .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(o -> o.getValue().getEstimatedTotalReward()), random))
            .getKey();
    }
}
