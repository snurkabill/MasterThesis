package vahy.impl.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.timer.SimpleTimer;

import java.util.List;


public abstract class AbstractTreeSearchPolicy<
        TAction extends Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
        TPolicyRecord extends PolicyRecord>
    implements Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);

    private final TreeUpdateCondition treeUpdateCondition;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    protected final SearchTreeImpl<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree;

    public AbstractTreeSearchPolicy(TreeUpdateCondition treeUpdateCondition,
                                    SearchTreeImpl<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchTree) {
        this.treeUpdateCondition = treeUpdateCondition;
        this.searchTree = searchTree;
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        for (TAction action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    protected void expandSearchTree(TState gameState) {
        checkStateRoot(gameState);
        timer.startTimer();
        treeUpdateCondition.treeUpdateRequired();
        for (int i = 0; treeUpdateCondition.isConditionSatisfied(); i++) {
            if(logger.isTraceEnabled()) {
                logger.trace("Performing tree update for [{}]th iteration", i);
            }
            searchTree.updateTree();
        }
        treeUpdateCondition.treeUpdateFinished();
        timer.stopTimer();
    }

    public abstract int getExpandedNodeCountSoFar();

    protected void checkStateRoot(TState gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree PaperPolicy has invalid state or argument itself is invalid. Possibly missing equals method");
        }
    }
}
