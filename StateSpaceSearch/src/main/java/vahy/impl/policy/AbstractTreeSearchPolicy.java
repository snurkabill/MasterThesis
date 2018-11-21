package vahy.impl.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.timer.SimpleTimer;

import java.util.List;


public abstract class AbstractTreeSearchPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements Policy<TAction, TReward, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);

    private final TreeUpdateCondition treeUpdateCondition;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    protected final SearchTreeImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchTree;

    public AbstractTreeSearchPolicy(TreeUpdateCondition treeUpdateCondition,
                                    SearchTreeImpl<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this.treeUpdateCondition = treeUpdateCondition;
        this.searchTree = searchTree;
    }

    @Override
    public void updateStateOnOpponentActions(List<TAction> opponentActionList) {
        for (TAction action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    protected void expandSearchTree(TState gameState) {
        checkStateRoot(gameState);
        timer.startTimer();
        treeUpdateCondition.treeUpdateRequired();
        for (int i = 0; treeUpdateCondition.isConditionSatisfied(); i++) {
            logger.trace("Performing tree update for [{}]th iteration", i);
            searchTree.updateTree();
        }
        treeUpdateCondition.treeUpdateFinished();
        timer.stopTimer();

        if (searchTree.getTotalNodesExpanded() == 0) {
            logger.debug("Finished updating search tree. No node was expanded - there is likely strong existing path to final state");
        } else {
            logger.debug(
                "Finished updating search tree with total expanded node count: [{}], " +
                    "total created node count: [{}], " +
                    "max branch factor: [{}], " +
                    "average branch factor [{}] in [{}] seconds, expanded nodes per second: [{}]",
                searchTree.getTotalNodesExpanded(),
                searchTree.getTotalNodesCreated(),
                searchTree.getMaxBranchingFactor(),
                searchTree.calculateAverageBranchingFactor(),
                timer.secondsSpent(),
                timer.samplesPerSec(searchTree.getTotalNodesExpanded()));
        }
    }

    private void checkStateRoot(TState gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree PaperPolicy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
    }
}
