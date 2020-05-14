package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.selector.RiskAverseNodeSelector;
import vahy.utils.EnumUtils;

import java.util.EnumMap;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperPolicySupplier<TAction extends Enum<TAction> & Action, TObservation extends Observation, TSearchNodeMetadata extends PaperMetadata<TAction>, TState extends PaperState<TAction, TObservation, TState>>
    implements PolicySupplier<TAction, TObservation, TState, PaperPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(PaperPolicySupplier.class.getName());

    private final int policyId;
    private final Class<TAction> actionClass;
    private final SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory;
    private final double totalRiskAllowedInference;
    private final SplittableRandom random;
    private final Supplier<RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState>> nodeSelectorSupplier;
    private final NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator;
    private final TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater;
    private final TreeUpdateConditionFactory treeUpdateConditionFactory;
    private final StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategiesProvider;

    // trainingSuppliers
    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    public PaperPolicySupplier(int policyId,
                               Class<TAction> actionClass,
                               SearchNodeMetadataFactory<TAction, TObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                               double totalRiskAllowedInference,
                               SplittableRandom random,
                               Supplier<RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState>> nodeSelectorSupplier,
                               NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                               TreeUpdater<TAction, TObservation, TSearchNodeMetadata, TState> treeUpdater,
                               TreeUpdateConditionFactory treeUpdateConditionFactory,
                               StrategiesProvider<TAction, TObservation, TSearchNodeMetadata, TState> strategiesProvider,
                               Supplier<Double> explorationConstantSupplier,
                               Supplier<Double> temperatureSupplier,
                               Supplier<Double> riskSupplier) {
        this.policyId = policyId;
        this.actionClass = actionClass;
        this.searchNodeMetadataFactory = searchNodeMetadataFactory;
        this.totalRiskAllowedInference = totalRiskAllowedInference;
        this.random = random;
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
    public Policy<TAction, TObservation, TState, PaperPolicyRecord> initializePolicy(StateWrapper<TAction, TObservation, TState> initialState, PolicyMode policyMode) {
        switch(policyMode) {
            case INFERENCE:
                return createPolicy(initialState);
            case TRAINING:
                return createPolicy(initialState, explorationConstantSupplier.get(), temperatureSupplier.get(), riskSupplier.get());
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode);
        }
    }

    protected PaperPolicy<TAction, TObservation, TState> createPolicy(StateWrapper<TAction, TObservation, TState> initialState) {
        logger.debug("Initialized INFERENCE policy. AllowedRisk: [{}]", totalRiskAllowedInference);
        var node = new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
        return new PaperPolicyImpl<>(
            policyId,
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                policyId,
                node,
                nodeSelectorSupplier.get(),
                treeUpdater,
                nodeEvaluator,
                random.split(),
                totalRiskAllowedInference,
                strategiesProvider),
            random.split());
    }

    protected PaperPolicy<TAction, TObservation, TState> createPolicy(StateWrapper<TAction, TObservation, TState> initialState, double explorationConstant, double temperature, double totalRiskAllowed) {
        logger.debug("Initialized TRAINING policy. Exploration constant: [{}], Temperature: [{}], Risk: [{}]", explorationConstant, temperature, totalRiskAllowed);
        var node = new SearchNodeImpl<>(initialState, searchNodeMetadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
        return new PaperPolicyImpl<>(
            policyId,
            actionClass,
            treeUpdateConditionFactory.create(),
            new RiskAverseSearchTree<>(
                policyId,
                node,
                nodeSelectorSupplier.get(),
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
