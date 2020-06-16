package vahy.impl.search.MCTS;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.search.node.SearchNode;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.impl.search.tree.SearchTreeImpl;

import java.util.SplittableRandom;

public class MCTSPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TObservation, MCTSMetadata, TState, PolicyRecordBase> {

    public MCTSPolicy(int policyId, SplittableRandom random, double explorationConstant, TreeUpdateCondition treeUpdateCondition, SearchTreeImpl<TAction, TObservation, MCTSMetadata, TState> searchTree) {
        super(policyId, random, explorationConstant, treeUpdateCondition, searchTree);
    }

//    private double[] innerActionProbabilityDistribution() {
//        var distribution = new double[countOfAllActions];
//        var root = searchTree.getRoot();
//        var actionIndex =  getBestAction(root.getStateWrapper(), root).ordinal();
//        distribution[actionIndex] = 1.0;
//        return distribution;
//    }

    @Override
    public TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        expandSearchTree(gameState);
        return super.getDiscreteAction(gameState);
//        return getBestAction(gameState, searchTree.getRoot());
    }

    @Override
    protected PlayingDistribution<TAction> inferenceBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        return getBestAction(gameState, searchTree.getRoot());
    }

    @Override
    protected PlayingDistribution<TAction> explorationBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        return getExploringAction(gameState, searchTree.getRoot());
    }

    private PlayingDistribution<TAction> getExploringAction(StateWrapper<TAction, TObservation, TState> gameState, SearchNode<TAction, TObservation, MCTSMetadata, TState> root) {
        var inGameId = gameState.getInGameEntityIdWrapper();
        TAction[] actions = gameState.getAllPossibleActions();
        var actionIndex = random.nextInt(actions.length);
        var action = actions[actionIndex];
        var applied = root.getChildNodeMap().get(action);
        MCTSMetadata metadata = applied.getSearchNodeMetadata();
        var value = metadata.getGainedReward()[inGameId] + metadata.getExpectedReward()[inGameId];
        return new PlayingDistribution<>(action, value, EMPTY_ARRAY);
    }

    private PlayingDistribution<TAction> getBestAction(StateWrapper<TAction, TObservation, TState> gameState, SearchNode<TAction, TObservation, MCTSMetadata, TState> root) {
        var inGameId = gameState.getInGameEntityIdWrapper();
        var childMap = root.getChildNodeMap();

        var max = -Double.MAX_VALUE;
        TAction bestAction = null;
        for (var entry : childMap.entrySet()) {
            var expectedReward = entry.getValue().getSearchNodeMetadata().getExpectedReward()[inGameId];
            var gainedReward = entry.getValue().getSearchNodeMetadata().getGainedReward()[inGameId];
            var actionValue = expectedReward + gainedReward;
            if(actionValue > max) {
                max = actionValue;
                bestAction = entry.getKey();
            }
        }
        return new PlayingDistribution<TAction>(bestAction, max, EMPTY_ARRAY);
//        return root.getChildNodeStream().max(Comparator.comparing(x -> x.getSearchNodeMetadata().getExpectedReward()[gameState.getInGameEntityIdWrapper()])).orElseThrow().getAppliedAction();
    }

    @Override
    public PolicyRecordBase getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        if(DEBUG_ENABLED) {
            checkStateRoot(gameState);
        }
        return new PolicyRecordBase(playingDistribution.getDistribution(), playingDistribution.getPredictedReward());
    }
}
