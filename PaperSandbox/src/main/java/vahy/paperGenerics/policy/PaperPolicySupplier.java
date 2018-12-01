package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.RiskAverseSearchTree;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class PaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>>
    implements PolicySupplier<TAction, TReward, TObservation, TState> {

    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowed;
    private final SplittableRandom random;
    private final NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;

    public PaperPolicySupplier(Class<TAction> actionClass,
                               SearchNodeMetadataFactory<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                               double totalRiskAllowed,
                               SplittableRandom random,
                               NodeSelector<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeSelector,
                               NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                               TreeUpdater<TAction, TReward, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory) {
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowed = totalRiskAllowed;
        this.random = random;
        this.nodeSelector = nodeSelector;
        this.nodeEvaluator = nodeEvaluator;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
    }

    @Override
    public PaperPolicy<TAction, TReward, TObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    protected Class<TAction> getActionClass() {
        return actionClass;
    }

    protected SplittableRandom getRandom() {
        return random;
    }

    protected PaperPolicy<TAction, TReward, TObservation, TState> createPolicy(TState initialState) {
        SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> node =
            new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new LinkedHashMap<>());
        return new PaperPolicyImpl<>(
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                node,
                nodeSelector,
                treeUpdater,
                nodeEvaluator,
                totalRiskAllowed),
            random);
    }
}
