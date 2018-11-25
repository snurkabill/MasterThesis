package vahy.paperGenerics;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.Policy;
import vahy.api.search.tree.SearchTree;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.List;

public class PaperPolicyImpl<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends DoubleVectorialObservation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends State<TAction, TReward, TObservation, TState>> implements Policy<TAction, TReward, TObservation, TState> {

    private final SearchTree<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchTree;

    public PaperPolicyImpl(SearchTree<TAction, TReward, TObservation, TSearchNodeMetadata, TState> searchTree) {
        this.searchTree = searchTree;
    }

    @Override
    public double[] getActionProbabilityDistribution(TState gameState) {
        return new double[0];
    }

    @Override
    public TAction getDiscreteAction(TState gameState) {

        return null;
    }

    @Override
    public void updateStateOnOpponentActions(List<TAction> opponentActionList) {

    }
}
