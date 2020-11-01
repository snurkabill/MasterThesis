package vahy.impl.policy.alphazero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.EnumUtils;
import vahy.utils.RandomDistributionUtils;

public class AlphaZeroEvaluator<
    TAction extends Enum<TAction> & Action,
    TState extends State<TAction, DoubleVector, TState>>
    extends AbstractNodeEvaluator<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(AlphaZeroEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();


    private final AlphaZeroNodeMetadataFactory<TAction, TState> searchNodeMetadataFactory;
    private final TrainablePredictor predictor;
    private final boolean isModelKnown;
    private PerfectStatePredictor<TAction, DoubleVector, TState> perfectEnvironmentPredictor;

    public AlphaZeroEvaluator(SearchNodeFactory<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> searchNodeFactory,
                              TrainablePredictor predictor,
                              boolean isModelKnown) {
        super(searchNodeFactory);
        this.searchNodeMetadataFactory = (AlphaZeroNodeMetadataFactory<TAction, TState>) searchNodeFactory.getSearchNodeMetadataFactory();
        this.predictor = predictor;
        this.isModelKnown = isModelKnown;
    }

    @Override
    protected int evaluateNode_inner(SearchNode<TAction, DoubleVector, AlphaZeroNodeMetadata<TAction>, TState> selectedNode) {

        var prediction = predictor.apply(selectedNode.getStateWrapper().getObservation());
        var entityInGameCount = selectedNode.getStateWrapper().getTotalEntityCount();

        var metadata = selectedNode.getSearchNodeMetadata();
        var expectedReward = metadata.getExpectedReward();

        System.arraycopy(prediction, 0, expectedReward, 0, expectedReward.length);

        if (!selectedNode.isFinalNode()) {
            var totalActionCount = searchNodeMetadataFactory.getTotalActionCount();
            double[] distribution = new double[totalActionCount];
            TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
            if (selectedNode.getStateWrapper().isEnvironmentEntityOnTurn() && isModelKnown) {
                useKnownModelPredictor(selectedNode, distribution, allPossibleActions);
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
        return 1;
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
}
