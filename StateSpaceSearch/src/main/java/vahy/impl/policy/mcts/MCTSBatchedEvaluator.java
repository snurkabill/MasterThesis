package vahy.impl.policy.mcts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.nodeEvaluator.AbstractBatchedNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;

public class MCTSBatchedEvaluator<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, DoubleVector, TState>>
    extends AbstractBatchedNodeEvaluator<TAction, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(MCTSBatchedEvaluator.class);
    protected static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    public MCTSBatchedEvaluator(SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory, TrainablePredictor predictor, int maximalEvaluationDepth) {
        super(searchNodeFactory, predictor, maximalEvaluationDepth);
    }

    @Override
    protected void fillNode(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> node, double[] valuePrediction) {
        var searchMetadata = node.getSearchNodeMetadata();
        var expectedReward = searchMetadata.getExpectedReward();
        System.arraycopy(valuePrediction, 0, expectedReward, 0, valuePrediction.length);    }
}
