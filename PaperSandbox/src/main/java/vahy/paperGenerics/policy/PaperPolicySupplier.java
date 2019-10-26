package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.utils.EnumUtils;

import java.util.LinkedHashMap;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements PolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicySupplier.class.getName());

    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowedInference;
    private final SplittableRandom random;
    private final Supplier<NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> nodeSelector;
    private final NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider;

    // trainingSuppliers
    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    public PaperPolicySupplier(Class<TAction> actionClass,
                               SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                               double totalRiskAllowedInference,
                               SplittableRandom random,
                               Supplier<NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> nodeSelector,
                               NodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                               TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory, StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider,
                               Supplier<Double> explorationConstantSupplier,
                               Supplier<Double> temperatureSupplier,
                               Supplier<Double> riskSupplier) {
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowedInference = totalRiskAllowedInference;
        this.random = random;
        this.nodeSelector = nodeSelector;
        this.nodeEvaluator = nodeEvaluator;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.strategiesProvider = strategiesProvider;
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.riskSupplier = riskSupplier;
    }

    @Override
    public Policy<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> initializePolicy(TState initialState, PolicyMode policyMode) {

        switch(policyMode) {
            case INFERENCE:
                return createPolicy(initialState);
            case TRAINING:
                return createPolicy(initialState, explorationConstantSupplier.get(), temperatureSupplier.get(), riskSupplier.get());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode);
        }
    }

    protected PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> createPolicy(TState initialState) {
        logger.debug("Initialized INFERENCE policy. AllowedRisk: [{}]", totalRiskAllowedInference);
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
        logger.debug("Initialized TRAINING policy. Exploration constant: [{}], Temperature: [{}], Risk: [{}]", explorationConstant, temperature, totalRiskAllowed);
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
