package vahy.paperGenerics.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.nodeSelector.EnvironmentSamplingNodeSelector;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public abstract class AbstractRiskAverseTreeBasedNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends State<TAction, TObservation, TState>>
    extends EnvironmentSamplingNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState>
    implements RiskAverseNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static final Logger logger = LoggerFactory.getLogger(PaperNodeSelector.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

    protected double allowedRiskInRoot;

    protected AbstractRiskAverseTreeBasedNodeSelector(SplittableRandom random, boolean isModelKnown) {
        super(random, isModelKnown);
    }

    @Override
    public void setAllowedRiskInRoot(double allowedRiskInRoot) {
        this.allowedRiskInRoot = allowedRiskInRoot;
    }

    @Override
    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        var node = root;
        while(!node.isLeaf()) {
            node = node.getChildNodeMap().get(getBestAction(node));
        }
        return node;
    }

}
