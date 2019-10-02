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
import vahy.impl.search.node.SearchNodeImpl;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.episode.PaperPolicyStepRecord;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyStepRecord> {

    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowedInference;
    private final SplittableRandom random;
    private final Supplier<NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> nodeSelector;
    private final NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider;

    public PaperPolicySupplier(Class<TAction> actionClass,
                               SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                               double totalRiskAllowedInference,
                               SplittableRandom random,
                               Supplier<NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> nodeSelector,
                               NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                               TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory, StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider) {
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowedInference = totalRiskAllowedInference;
        this.random = random;
        this.nodeSelector = nodeSelector;
        this.nodeEvaluator = nodeEvaluator;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.strategiesProvider = strategiesProvider;
    }

    @Override
    public PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    protected PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> createPolicy(TState initialState) {
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node =
            new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new LinkedHashMap<>());
        return new PaperPolicyImpl<>(
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                actionClass,
                node,
                nodeSelector.get(),
                treeUpdater,
                nodeEvaluator,
                random.split(),
                totalRiskAllowedInference,
                strategiesProvider),
            random.split());
    }

    protected PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> createPolicy(TState initialState, double explorationConstant, double temperature, double totalRiskAllowed) {
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node =
            new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new LinkedHashMap<>());
        return new PaperPolicyImpl<>(
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                actionClass,
                node,
                nodeSelector.get(),
                treeUpdater,
                nodeEvaluator,
                random.split(),
                totalRiskAllowed,
                strategiesProvider),
            random.split(),
            explorationConstant,
            temperature);
    }
}
