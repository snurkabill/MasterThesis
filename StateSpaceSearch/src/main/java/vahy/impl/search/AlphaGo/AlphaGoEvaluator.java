package vahy.impl.search.AlphaGo;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;

import java.util.function.Function;

public class AlphaGoEvaluator<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends DoubleVector,
    TSearchNodeMetadata extends AlphaGoNodeMetadata<TAction>,
    TState extends State<TAction, TObservation, TState>>
    implements NodeEvaluator<TAction, TObservation, TSearchNodeMetadata, TState> {

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
    public int evaluateNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectedNode) {
        throw new UnsupportedOperationException(); // TODO: finish it
    }
}
