package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.SplittableRandom;

public abstract class AbstractRiskAverseTreeBasedNodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>
    implements RiskAverseNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected final SplittableRandom random;
    protected double allowedRiskInRoot;

    protected AbstractRiskAverseTreeBasedNodeSelector(SplittableRandom random) {
        this.random = random;
    }

    protected final TAction sampleOpponentAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
        var actions = new ArrayList<TAction>(node.getChildNodeMap().size());
        var priorProbabilities = new double[node.getChildNodeMap().size()];
        int index = 0;
        for(var entry : node.getChildNodeMap().values()) {
            actions.add(entry.getAppliedAction());
            priorProbabilities[index] = entry.getSearchNodeMetadata().getPriorProbability();
            index++;
        }
        int randomIndex = RandomDistributionUtils.getRandomIndexFromDistribution(priorProbabilities, random);
        return actions.get(randomIndex);
    }

    @Override
    public void setAllowedRiskInRoot(double allowedRiskInRoot) {
        this.allowedRiskInRoot = allowedRiskInRoot;
    }
}
