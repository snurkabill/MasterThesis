package vahy.impl.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.timer.SimpleTimer;

import java.util.List;


public abstract class AbstractTreeSearchPolicy<
        TAction extends Enum<TAction> & Action,
        TObservation extends Observation,
        TSearchNodeMetadata extends SearchNodeMetadata,
        TState extends State<TAction, TObservation, TState>,
        TPolicyRecord extends PolicyRecord>
    implements Policy<TAction, TObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTreeSearchPolicy.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    private final TreeUpdateCondition treeUpdateCondition;
    private final SimpleTimer timer = new SimpleTimer(); // TODO: take as arg in constructor

    protected final int policyId;
    protected final SearchTreeImpl<TAction, TObservation, TSearchNodeMetadata, TState> searchTree;

    public AbstractTreeSearchPolicy(int policyId,
                                    TreeUpdateCondition treeUpdateCondition,
                                    SearchTreeImpl<TAction, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this.policyId = policyId;
        this.treeUpdateCondition = treeUpdateCondition;
        this.searchTree = searchTree;
    }

    @Override
    public int getPolicyId() {
        return policyId;
    }

    @Override
    public void updateStateOnPlayedActions(List<TAction> opponentActionList) {
        for (TAction action : opponentActionList) {
            searchTree.applyAction(action);
        }
    }

    protected void expandSearchTree(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);
        timer.startTimer();
        treeUpdateCondition.treeUpdateRequired();
        for (int i = 0; treeUpdateCondition.isConditionSatisfied(); i++) {
            if(TRACE_ENABLED) {
                logger.trace("Performing tree update for [{}]th iteration", i);
            }
            searchTree.updateTree();
        }
        treeUpdateCondition.treeUpdateFinished();
        timer.stopTimer();
    }

    protected void checkStateRoot(StateWrapper<TAction, TObservation, TState> gameState) {
        if (!searchTree.getRoot().getWrappedState().equals(gameState)) {
            throw new IllegalStateException("Tree PaperPolicy has invalid state in root or gameState argument itself is invalid. Possible issues: " + System.lineSeparator() +
                "1. missing or invalid equals method on state implementation. Proper equals method does not have to take into account static parts of state" + System.lineSeparator() +
                "2. wrong logic in applying actons on states, leading in inconsistency" + System.lineSeparator() +
                "Expected state as string: " + System.lineSeparator() + searchTree.getRoot().getWrappedState().getWrappedState().readableStringRepresentation() + System.lineSeparator() +
                "Actual state as string: " + System.lineSeparator() + gameState.getWrappedState().readableStringRepresentation());
        }
    }
}
