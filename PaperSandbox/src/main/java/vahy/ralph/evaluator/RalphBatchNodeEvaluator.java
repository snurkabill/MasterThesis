package vahy.ralph.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractBatchedNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.metadata.RiskSearchMetadataFactory;
import vahy.utils.EnumUtils;
import vahy.utils.RandomDistributionUtils;

public class RalphBatchNodeEvaluator<
    TAction extends Enum<TAction> & Action,
    TState extends RiskState<TAction, DoubleVector, TState>>
    extends AbstractBatchedNodeEvaluator<TAction, RalphMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(RalphBatchNodeEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();


    private final RiskSearchMetadataFactory<TAction, DoubleVector, TState> searchNodeMetadataFactory;
    private final boolean isModelKnown;
    private PerfectStatePredictor<TAction, DoubleVector, TState> perfectEnvironmentPredictor;

    public RalphBatchNodeEvaluator(SearchNodeFactory<TAction, DoubleVector, RalphMetadata<TAction>, TState> searchNodeFactory,
                                   TrainablePredictor predictor,
                                   int maximalEvaluationDepth,
                                   boolean isModelKnown) {
        super(searchNodeFactory, predictor, maximalEvaluationDepth);
        this.searchNodeMetadataFactory = (RiskSearchMetadataFactory<TAction, DoubleVector, TState>) searchNodeFactory.getSearchNodeMetadataFactory();
        this.isModelKnown = isModelKnown;
    }

    private void useKnownModelPredictor(SearchNode<TAction, DoubleVector, RalphMetadata<TAction>, TState> selectedNode, double[] distribution, TAction[] allPossibleActions) {
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
    protected void fillNode(SearchNode<TAction, DoubleVector, RalphMetadata<TAction>, TState> selectedNode, double[] prediction) {
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
    }
}
