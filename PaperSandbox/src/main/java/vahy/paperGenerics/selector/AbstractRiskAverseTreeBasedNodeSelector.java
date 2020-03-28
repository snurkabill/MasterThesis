package vahy.paperGenerics.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public abstract class AbstractRiskAverseTreeBasedNodeSelector<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>
    implements RiskAverseNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static Logger logger = LoggerFactory.getLogger(PaperNodeSelector.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

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
        TAction action = actions.get(randomIndex);
        if(TRACE_ENABLED) {
            logger.trace("Sampled [{}] action from opponent's actions: [{}] by random index [{}] from [{}] distribution.", action, actions.stream().map(Enum::toString).collect(Collectors.joining(", ")), randomIndex, Arrays.toString(priorProbabilities));
        }
        return action;
    }

    @Override
    public void setAllowedRiskInRoot(double allowedRiskInRoot) {
        this.allowedRiskInRoot = allowedRiskInRoot;
    }
}
