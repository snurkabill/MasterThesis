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
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.SplittableRandom;
import java.util.function.Supplier;

public class TrainablePaperPolicySupplier<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(TrainablePaperPolicySupplier.class.getName());

    private final Supplier<Double> expplorationConstantSupplier;
    private final Supplier<Double> temperatureSupplier;

    public TrainablePaperPolicySupplier(Class<TAction> actionClass,
                                        SearchNodeMetadataFactory<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> searchNodeMetadataFactory,
                                        double totalRiskAllowed,
                                        SplittableRandom random,
                                        NodeSelector<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeSelector,
                                        TrainableNodeEvaluator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> nodeEvaluator,
                                        TreeUpdater<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> treeUpdater,
                                        TreeUpdateConditionFactory treeUpdateConditionFactory,
                                        Supplier<Double> explorationConstantSupplier,
                                        Supplier<Double> temperatureSupplier) {
        super(actionClass, searchNodeMetadataFactory, totalRiskAllowed, random, nodeSelector, nodeEvaluator, treeUpdater, treeUpdateConditionFactory);
        this.expplorationConstantSupplier = explorationConstantSupplier;
        this.temperatureSupplier = temperatureSupplier;
    }

    public PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initializePolicy(TState initialState) {
        return createPolicy(initialState);
    }

    public PaperPolicyImplWithExploration<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initializePolicyWithExploration(TState initialState) {
        double explorationConstant = expplorationConstantSupplier.get();
        double temperature = temperatureSupplier.get();
        logger.info("Initialized policy with exploration. Exploration constant: [{}], Temperature: [{}]", explorationConstant, temperature);
        return new PaperPolicyImplWithExploration<>(getActionClass(), getRandom(), createPolicy(initialState), explorationConstant, temperature);
    }

}
