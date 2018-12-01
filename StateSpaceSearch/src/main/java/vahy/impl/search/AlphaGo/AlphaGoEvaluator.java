package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;

import java.util.function.Function;

public class AlphaGoEvaluator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends AlphaGoNodeMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    implements NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    public static final int Q_VALUE_INDEX = 0;
    public static final int POLICY_START_INDEX = 1;

    private final Function<DoubleVector, double[]> functionApproximator;

    public AlphaGoEvaluator(Function<DoubleVector, double[]> functionApproximator) {
        this.functionApproximator = functionApproximator;
    }

    public Function<DoubleVector, double[]> getFunctionApproximator() {
        return functionApproximator;
    }

    @Override
    public void evaluateNode(SearchNode<TAction, TReward, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        throw new UnsupportedOperationException(); // TODO: finish it
    }
}
