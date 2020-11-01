package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.utils.EnumUtils;

import java.util.EnumMap;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperPolicySupplierImpl<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TSearchNodeMetadata extends PaperMetadata<TAction>, TState extends PaperState<TAction, TObservation, TState>>
    implements OuterDefPolicySupplier<TAction, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicySupplierImpl.class.getName());
    private static final boolean DEBUG_ENABLED = logger.isDebugEnabled();

    private final Class<TAction> actionClass;
    private final SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory;
    private final SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowedInference;
    private final Supplier<RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState>> nodeSelectorSupplier;
    private final NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategiesProvider;

    // trainingSuppliers
    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    public PaperPolicySupplierImpl(Class<TAction> actionClass,
                                   SearchNodeFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeFactory,
                                   SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                                   double totalRiskAllowedInference,
                                   Supplier<RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState>> nodeSelectorSupplier,
                                   NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                   TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                                   TreeUpdateConditionFactory treeUpdateConditionFactory,
                                   StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategiesProvider,
                                   Supplier<Double> explorationConstantSupplier,
                                   Supplier<Double> temperatureSupplier,
                                   Supplier<Double> riskSupplier) {
        this.actionClass = actionClass;
        this.searchNodeFactory = searchNodeFactory;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowedInference = totalRiskAllowedInference;
        this.nodeSelectorSupplier = nodeSelectorSupplier;
        this.nodeEvaluator = nodeEvaluator;
        this.treeUpdater = treeUpdater;
        this.treeUpdateConditionFactory = treeUpdateConditionFactory;
        this.strategiesProvider = strategiesProvider;
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.riskSupplier = riskSupplier;
    }

    @Override
    public Policy<TAction, TObservation, TState> apply(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
        switch(policyMode) {
            case INFERENCE:
                return createPolicy(initialState, random, policyId);
            case TRAINING:
                return createPolicy(initialState, random, policyId, explorationConstantSupplier.get(), temperatureSupplier.get(), riskSupplier.get());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode);
        }
    }

    protected Policy<TAction, TObservation, TState> createPolicy(StateWrapper<TAction, TObservation, TState> initialState, SplittableRandom random, int policyId) {
        if(DEBUG_ENABLED) {
            logger.debug("Initialized INFERENCE policy. AllowedRisk: [{}]", totalRiskAllowedInference);
        }
        var node = searchNodeFactory.createNode(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
        return new PaperPolicyImpl<>(
            policyId,
            random,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                searchNodeFactory,
                node,
                nodeSelectorSupplier.get(),
                treeUpdater,
                nodeEvaluator,
                random,
                totalRiskAllowedInference,
                strategiesProvider));
    }

    protected Policy<TAction, TObservation, TState> createPolicy(StateWrapper<TAction, TObservation, TState> initialState, SplittableRandom random, int policyId, double explorationConstant, double temperature, double totalRiskAllowed) {
        if(DEBUG_ENABLED) {
            logger.debug("Initialized TRAINING policy. Exploration constant: [{}], Temperature: [{}], Risk: [{}]", explorationConstant, temperature, totalRiskAllowed);
        }
        var node = searchNodeFactory.createNode(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
        return new PaperPolicyImpl<>(
            policyId,
            random,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                searchNodeFactory,
                node,
                nodeSelectorSupplier.get(),
                treeUpdater,
                nodeEvaluator,
                random,
                totalRiskAllowed,
                strategiesProvider),
            explorationConstant,
            temperature);
    }
}
