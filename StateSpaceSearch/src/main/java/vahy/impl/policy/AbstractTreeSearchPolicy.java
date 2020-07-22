package vahy.impl.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.ExploringPolicy;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.search.tree.SearchTreeImpl;

import java.util.SplittableRandom;


public abstract class AbstractTreeSearchPolicy<
        TAction extends Enum<TAction> & Action,
        TObservation extends Observation,
        TSearchNodeMetadata extends NodeMetadata,
        TState extends State<TAction, TObservation, TState>>
    extends ExploringPolicy<TAction, TObservation, TState> {

    private final TreeUpdateCondition treeUpdateCondition;

    protected final SearchTreeImpl<TAction, TObservation, TSearchNodeMetadata, TState> searchTree;

//    protected final int countOfAllActionFromSameEntity;
    protected final int countOfAllActions;

    public AbstractTreeSearchPolicy(int policyId, SplittableRandom random, double exploringConstant, TreeUpdateCondition treeUpdateCondition, SearchTreeImpl<TAction, TObservation, TSearchNodeMetadata, TState> searchTree) {
        super(random, policyId, exploringConstant);
        this.treeUpdateCondition = treeUpdateCondition;
        this.searchTree = searchTree;
//        this.countOfAllActionFromSameEntity = obtainCountOfAllActionFromSameEntity(searchTree.getRoot());
        this.countOfAllActions = obtainCountOfAllActions(searchTree.getRoot());
    }

//    private int obtainCountOfAllActionFromSameEntity(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> gameState) {
//        var actionArray = gameState.getAllPossibleActions();
//        if(actionArray.length == 0) {
//            throw new IllegalStateException("There must be at least one playable action.");
//        }
//        return actionArray[0].getCountOfAllActionsFromSameEntity();
//    }

    private int obtainCountOfAllActions(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> gameState) {
        var actionArray = gameState.getAllPossibleActions();
        if(actionArray.length == 0) {
            throw new IllegalStateException("There must be at least one playable action.");
        }
        return actionArray[0].getDeclaringClass().getEnumConstants().length;
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        expandSearchTree(gameState);
        return super.getDiscreteAction(gameState);
    }

    @Override
    public void updateStateOnPlayedAction(TAction action) {
        searchTree.applyAction(action);
    }

    protected void expandSearchTree(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        treeUpdateCondition.treeUpdateRequired();
        for (int i = 0; treeUpdateCondition.isConditionSatisfied(); i++) {
            if(TRACE_ENABLED) {
                logger.trace("Performing tree update for [{}]th iteration", i);
            }
            searchTree.expandTree();
        }
        treeUpdateCondition.treeUpdateFinished();
    }

    protected void checkStateRoot(StateWrapper<TAction, TObservation, TState> gameState) {
        if (!searchTree.getRoot().getStateWrapper().wrappedStatesEquals(gameState)) {
            throw new IllegalStateException("Tree Policy has invalid state in root or gameState argument itself is invalid. Possible issues: " + System.lineSeparator() +
                "1. missing or invalid equals method on state implementation. Proper equals method does not have to take into account static parts of state" + System.lineSeparator() +
                "2. wrong logic in applying actons on states, leading in inconsistency" + System.lineSeparator() +
                "Expected state as string: " + System.lineSeparator() + searchTree.getRoot().getStateWrapper().getReadableStringRepresentation() + System.lineSeparator() +
                "Actual state as string: " + System.lineSeparator() + gameState.getReadableStringRepresentation());
        }
    }
}
