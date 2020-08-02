package vahy.paperGenerics.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.predictor.Predictor;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.utils.EnumUtils;
import vahy.utils.RandomDistributionUtils;

public class PaperNodeEvaluator<TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>>
    extends AbstractNodeEvaluator<TAction, DoubleVector, PaperMetadata<TAction>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperNodeEvaluator.class);
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;

    private final PaperMetadataFactory<TAction, DoubleVector, TState> searchNodeMetadataFactory;
    private final TrainablePredictor trainablePredictor;
    private final boolean isModelKnown;
    private Predictor<TState> perfectEnvironmentPredictor;

    @SuppressWarnings("unchecked")
    public PaperNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, PaperMetadata<TAction>, TState> searchNodeFactory, TrainablePredictor trainablePredictor, boolean isModelKnown) {
        super(searchNodeFactory);
        this.searchNodeMetadataFactory = (PaperMetadataFactory<TAction, DoubleVector, TState>)searchNodeFactory.getSearchNodeMetadataFactory();
        this.trainablePredictor = trainablePredictor;
        this.isModelKnown = isModelKnown;
    }

    @Override
    protected int evaluateNode_inner(SearchNode<TAction, DoubleVector, PaperMetadata<TAction>, TState> selectedNode) {
        var prediction = trainablePredictor.apply(selectedNode.getStateWrapper().getObservation());
        var entityInGameCount = selectedNode.getStateWrapper().getTotalEntityCount();

        var metadata = selectedNode.getSearchNodeMetadata();
        var expectedReward = metadata.getExpectedReward();
        var expectedRisk = metadata.getExpectedRisk();

        System.arraycopy(prediction, 0, expectedReward, 0, expectedReward.length);
        System.arraycopy(prediction, entityInGameCount, expectedRisk, 0, expectedRisk.length);

        if (!selectedNode.isFinalNode()) {
            var totalActionCount = searchNodeMetadataFactory.getTotalActionCount();
            double[] distribution = new double[totalActionCount];
            TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
            if (selectedNode.getStateWrapper().isEnvironmentEntityOnTurn() && isModelKnown) {
                useKnownModelPredictor(selectedNode, distribution, allPossibleActions);
            } else {
                System.arraycopy(prediction, entityInGameCount * 2, distribution, 0, distribution.length);
                boolean[] mask = EnumUtils.createMask(allPossibleActions, totalActionCount);
                RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);
            }

            var childPriorProbabilities = metadata.getChildPriorProbabilities();
            for (TAction key : allPossibleActions) {
                childPriorProbabilities.put(key, distribution[key.ordinal()]);
            }
        }
        return 1;
    }

    private void useKnownModelPredictor(SearchNode<TAction, DoubleVector, PaperMetadata<TAction>, TState> selectedNode, double[] distribution, TAction[] allPossibleActions) {
        if (perfectEnvironmentPredictor == null) {
            perfectEnvironmentPredictor = selectedNode.getStateWrapper().getKnownModelWithPerfectObservationPredictor();
        }
        var modelPrediction = perfectEnvironmentPredictor.apply(selectedNode.getStateWrapper().getWrappedState());

        if (modelPrediction.length != allPossibleActions.length) {
            throw new IllegalStateException("Inconsistency between array lengths");
        }
        for (int i = 0; i < modelPrediction.length; i++) {
            distribution[allPossibleActions[i].ordinal()] = modelPrediction[i];
        }
    }
}
