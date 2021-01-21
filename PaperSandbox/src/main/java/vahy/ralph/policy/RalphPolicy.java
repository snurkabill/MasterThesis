package vahy.ralph.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskPolicyRecord;
import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.ralph.metadata.RalphMetadata;

import java.util.SplittableRandom;

public class RalphPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractTreeSearchPolicy<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(RalphPolicy.class.getName());
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();

    private final RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> riskAverseSearchTree;

    private final double temperature;

    public RalphPolicy(int policyId,
                       SplittableRandom random,
                       TreeUpdateCondition treeUpdateCondition,
                       RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this(policyId, random, treeUpdateCondition, searchTree, 0.0, 0.0);
    }

    public RalphPolicy(int policyId,
                       SplittableRandom random,
                       TreeUpdateCondition treeUpdateCondition,
                       RiskAverseSearchTree<TAction, TObservation, TSearchNodeMetadata, TState> searchTree,
                       double explorationConstant,
                       double temperature) {
        super(policyId, random, explorationConstant, treeUpdateCondition, searchTree);
        this.riskAverseSearchTree = searchTree;
        this.temperature = temperature;
    }

    @Override
    protected PlayingDistribution<TAction> inferenceBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        var distributionWithRisk = riskAverseSearchTree.inferencePolicyBranch();
        return distributionWithRisk;
    }

    @Override
    protected PlayingDistribution<TAction> explorationBranch(StateWrapper<TAction, TObservation, TState> gameState) {
        var distributionWithRisk = riskAverseSearchTree.explorationPolicyBranch(temperature);
        return distributionWithRisk;
    }

    @Override
    public RiskPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState) {
        checkStateRoot(gameState);

        var distribution = new double[countOfAllActions];
        distribution[playingDistribution.getPlayedAction().ordinal()] = 1.0;
        return new RiskPolicyRecord(
            distribution,
            playingDistribution.getExpectedReward(),
            riskAverseSearchTree.getRoot().getSearchNodeMetadata().getExpectedRisk()[gameState.getInGameEntityId()],
            riskAverseSearchTree.getTotalRiskAllowed(),
            riskAverseSearchTree.getTotalNodesExpanded());
    }

}
