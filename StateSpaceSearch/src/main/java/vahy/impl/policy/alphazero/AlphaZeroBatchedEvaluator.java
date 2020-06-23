package vahy.impl.policy.alphazero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.predictor.Predictor;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractBatchedNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.EnumUtils;
import vahy.utils.RandomDistributionUtils;

public class AlphaZeroBatchedEvaluator<
    TAction extends Enum<TAction> & Action,
    TState extends State<TAction, DoubleVector, TState>>
    extends AbstractBatchedNodeEvaluator<TAction, AlphaZeroNodeMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(AlphaZeroBatchedEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();


    private final AlphaZeroNodeMetadataFactory<TAction, DoubleVector, TState> searchNodeMetadataFactory;
    private final boolean isModelKnown;
    private Predictor<TState> perfectEnvironmentPredictor;

    public AlphaZeroBatchedEvaluator(SearchNodeFactory<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> searchNodeFactory,
                                     TrainablePredictor predictor,
                                     int maximalEvaluationDepth,
                                     boolean isModelKnown) {
        super(searchNodeFactory, predictor, maximalEvaluationDepth);
        this.searchNodeMetadataFactory = (AlphaZeroNodeMetadataFactory<TAction, DoubleVector, TState>) searchNodeFactory.getSearchNodeMetadataFactory();
        this.isModelKnown = isModelKnown;
    }

    private void useKnownModelPredictor(SearchNode<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> selectedNode, double[] distribution, TAction[] allPossibleActions) {
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

    @Override
    protected void fillNode(SearchNode<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> node, double[] prediction) {
        var entityInGameCount = node.getStateWrapper().getTotalEntityCount();

        var metadata = node.getSearchNodeMetadata();
        var expectedReward = metadata.getExpectedReward();

        System.arraycopy(prediction, 0, expectedReward, 0, expectedReward.length);

        if (!node.isFinalNode()) {
            var totalActionCount = searchNodeMetadataFactory.getTotalActionCount();
            double[] distribution = new double[totalActionCount];
            TAction[] allPossibleActions = node.getAllPossibleActions();
            if (node.getStateWrapper().isEnvironmentEntityOnTurn() && isModelKnown) {
                useKnownModelPredictor(node, distribution, allPossibleActions);
            } else {
                System.arraycopy(prediction, entityInGameCount, distribution, 0, distribution.length);
                boolean[] mask = EnumUtils.createMask(allPossibleActions, totalActionCount);
                RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);
            }

            var childPriorProbabilities = metadata.getChildPriorProbabilities();
            for (TAction key : allPossibleActions) {
                childPriorProbabilities.put(key, distribution[key.ordinal()]);
            }
        }
    }
}
