package vahy.impl.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.timer.SimpleTimer;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;


public abstract class AbstractTreeSearchPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>>
    implements Policy<TAction, TReward, TObservation> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);

    private final SplittableRandom random;
    private final SearchTreeImpl<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchTree;
    private final int updateTreeCount;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    public AbstractTreeSearchPolicy(SplittableRandom random, int updateTreeCount, SearchTreeImpl<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, State<TAction, TReward, TObservation>> searchTree) {
        this.random = random;
        this.updateTreeCount = updateTreeCount;
        this.searchTree = searchTree;
    }

    @Override
    public void updateStateOnOpponentActions(List<TAction> opponentActionList) {
        for (TAction action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    @Override
    public TAction getDiscreteAction(State<TAction, TReward, TObservation> gameState) {
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
    public double[] getActionProbabilityDistribution(State<TAction, TReward, TObservation> gameState) {
        throw new UnsupportedOperationException("I will implement this when it will be needed.");
    }

    private void expandSearchTree(State<TAction, TReward, TObservation> gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree Policy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
        timer.startTimer();
        for (int i = 0; i < updateTreeCount; i++) {
            logger.trace("Performing tree update for [{}]th iteration", i);
            searchTree.updateTree();
        }
        timer.stopTimer();

        if (searchTree.getTotalNodesExpanded() == 0) {
            logger.info("Finished updating search tree. No node was expanded - there is likely strong existing path to final state");
        } else {
            logger.info("Finished updating search tree with total expanded node count: [{}], total created node count: [{}],  max branch factor: [{}], average branch factor [{}] in [{}] seconds, expanded nodes per second: [{}]",
                searchTree.getTotalNodesExpanded(),
                searchTree.getTotalNodesCreated(),
                searchTree.getMaxBranchingFactor(),
                searchTree.calculateAverageBranchingFactor(),
                timer.secondsSpent(),
                timer.samplesPerSec(searchTree.getTotalNodesExpanded()));
        }

//        logger.trace("Action estimatedRewards: [{}]", searchTree
//            .getRoot()
//            .getSearchNodeMetadata()
//            .getStateActionMetadataMap()
//            .entrySet()
//            .stream()
//            .map(x -> String.valueOf(x.getValue().getEstimatedTotalReward().getValue().doubleValue()))
//            .reduce((x, y) -> x + ", " + y));
    }
}
