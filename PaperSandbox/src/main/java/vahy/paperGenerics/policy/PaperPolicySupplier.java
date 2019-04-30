package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.riskSubtree.RiskAverseSearchTree;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class PaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> {

    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowed;
    private final SplittableRandom random;
    private final NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector;
    private final NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final StrategiesProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider;

    public PaperPolicySupplier(Class<TAction> actionClass,
                               SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                               double totalRiskAllowed,
                               SplittableRandom random,
                               NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                               NodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                               TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory, StrategiesProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider) {
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowed = totalRiskAllowed;
        this.random = random;
        this.nodeSelector = nodeSelector;
        this.nodeEvaluator = nodeEvaluator;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.strategiesProvider = strategiesProvider;
    }

    @Override
    public PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    protected Class<TAction> getActionClass() {
        return actionClass;
    }

    protected SplittableRandom getRandom() {
        return random;
    }

    protected PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> createPolicy(TState initialState) {
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node =
            new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new LinkedHashMap<>());
        return new PaperPolicyImpl<>(
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                actionClass,
                node,
                nodeSelector,
                treeUpdater,
                nodeEvaluator,
                random,
                totalRiskAllowed,
                strategiesProvider),
            random);
    }

    protected PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> createPolicy(TState initialState, double explorationConstant, double temperature) {
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node =
            new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new LinkedHashMap<>());
        return new PaperPolicyImpl<>(
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                actionClass,
                node,
                nodeSelector,
                treeUpdater,
                nodeEvaluator,
                random,
                totalRiskAllowed,
                strategiesProvider),
            random,
            explorationConstant,
            temperature);
    }
}
