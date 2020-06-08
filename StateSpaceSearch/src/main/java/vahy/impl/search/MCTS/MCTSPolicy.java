package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.search.node.SearchNode;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.impl.search.tree.SearchTreeImpl;

import java.util.Comparator;
import java.util.SplittableRandom;

public class MCTSPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TObservation, MCTSMetadata, TState, PolicyRecordBase> {

    public MCTSPolicy(int policyId, SplittableRandom random, TreeUpdateCondition treeUpdateCondition, SearchTreeImpl<TAction, TObservation, MCTSMetadata, TState> searchTree) {
        super(policyId, random, treeUpdateCondition, searchTree);
    }

    private double[] innerActionProbabilityDistribution() {
        var distribution = new double[countOfAllActionFromSameEntity];
        var root = searchTree.getRoot();
        var actionIndex =  getBestAction(root.getStateWrapper(), root).getLocalIndex();
        distribution[actionIndex] = 1.0;
        return distribution;
    }

    @Override
    public double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        return innerActionProbabilityDistribution();
    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        expandSearchTree(gameState);
        return getBestAction(gameState, searchTree.getRoot());
    }

    private TAction getBestAction(StateWrapper<TAction, TObservation, TState> gameState, SearchNode<TAction, TObservation, MCTSMetadata, TState> root) {
        return root.getChildNodeStream().max(Comparator.comparing(x -> x.getSearchNodeMetadata().getExpectedReward()[gameState.getInGameEntityIdWrapper()])).orElseThrow().getAppliedAction();
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        return new PolicyRecordBase(innerActionProbabilityDistribution(), searchTree.getRoot().getSearchNodeMetadata().getExpectedReward()[gameState.getInGameEntityIdWrapper()]);
    }
}
