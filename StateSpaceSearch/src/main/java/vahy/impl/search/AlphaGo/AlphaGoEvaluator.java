package vahy.impl.search.AlphaGo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;

public class AlphaGoEvaluator<
    TAction extends Enum<TAction> & Action,
    TObservation extends DoubleVector,
    TState extends State<TAction, TObservation, TState>>
    implements NodeEvaluator<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(AlphaGoEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    public static final int Q_VALUE_INDEX = 0;

    private final SearchNodeFactory<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> searchNodeFactory;
    private final AlphaGoNodeMetadataFactory<TAction, TObservation, TState> searchNodeMetadataFactory;
    private final TrainablePredictor predictor;


    public AlphaGoEvaluator(SearchNodeFactory<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> searchNodeFactory, TrainablePredictor predictor) {
        this.searchNodeFactory = searchNodeFactory;
        this.searchNodeMetadataFactory = (AlphaGoNodeMetadataFactory<TAction, TObservation, TState>) searchNodeFactory.getSearchNodeMetadataFactory();
        this.predictor = predictor;
    }

    @Override
    public int evaluateNode(SearchNode<TAction, TObservation, AlphaGoNodeMetadata<TAction>, TState> selectedNode) {
        if(selectedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be expanded.");
        }
        TAction[] allPossibleActions = selectedNode.getAllPossibleActions();
        if(TRACE_ENABLED) {
            logger.trace("Expanding node [{}] with possible actions: [{}] ", selectedNode, Arrays.toString(allPossibleActions));
        }
        var prediction = predictor.apply(selectedNode.getStateWrapper().getObservation());
        var entityInGameCount = selectedNode.getStateWrapper().getTotalEntityCount();

        var metadata = selectedNode.getSearchNodeMetadata();
        var predictedReward = metadata.getPredictedReward();

        System.arraycopy(prediction, Q_VALUE_INDEX, predictedReward, 0, predictedReward.length);

        var totalActionCount = searchNodeMetadataFactory.getTotalActionCount();
        double[] distribution = new double[totalActionCount];
        System.arraycopy(prediction, entityInGameCount, distribution, 0, distribution.length);

        boolean[] mask = new boolean[totalActionCount];
        for (int i = 0; i < allPossibleActions.length; i++) {
            mask[allPossibleActions[i].ordinal()] = true;
        }
        RandomDistributionUtils.applyMaskToRandomDistribution(distribution, mask);

        var childPriorProbabilities = metadata.getChildPriorProbabilities();
        for (TAction key : allPossibleActions) {
            childPriorProbabilities.put(key, distribution[key.ordinal()]);
        }

        var childNodeMap = selectedNode.getChildNodeMap();
        for (TAction nextAction : allPossibleActions) {
            var stateRewardReturn = selectedNode.applyAction(nextAction);
            childNodeMap.put(nextAction, searchNodeFactory.createNode(stateRewardReturn, selectedNode, nextAction));
        }

        if(!selectedNode.isFinalNode()) {
            selectedNode.unmakeLeaf();
        }
        return 1;
    }
}
