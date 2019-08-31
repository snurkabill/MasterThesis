package vahy.paperGenerics.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;
import vahy.api.search.update.TreeUpdater;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;

import java.util.SplittableRandom;
import java.util.function.Supplier;

public class TrainablePaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(TrainablePaperPolicySupplier.class.getName());

    private final Supplier<Double> explorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;
    private final Supplier<Double> riskSupplier;

    public TrainablePaperPolicySupplier(Class<TAction> actionClass,
                                        SearchNodeMetadataFactory<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                                        double totalRiskAllowed,
                                        SplittableRandom random,
                                        Supplier<NodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> nodeSelector,
                                        TrainableNodeEvaluator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                        TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                        TreeUpdateConditionFactory treeUpdateConditionFactory,
                                        Supplier<Double> explorationConstantSupplier,
                                        Supplier<Double> temperatureSupplier,
                                        Supplier<Double> riskSupplier,
                                        StrategiesProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> strategiesProvider) {
        super(actionClass, searchNodeMetadataFactory, totalRiskAllowed, random, nodeSelector, nodeEvaluator, treeUpdater, treeUpdateConditionFactory, strategiesProvider);
        this.explorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
        this.riskSupplier = riskSupplier;
    }

    public PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    public PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> initializePolicyWithExploration(TState initialState) {
        double explorationConstant = explorationConstantSupplier.get();
        double temperature = temperatureSupplier.get();
        double risk = riskSupplier.get();
        logger.debug("Initialized policy with exploration. Exploration constant: [{}], Temperature: [{}], Risk: [{}]", explorationConstant, temperature);
        return createPolicy(initialState, explorationConstant, temperature, risk);
    }

}
