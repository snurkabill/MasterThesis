package vahy.noLinear.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.TreeUpdater;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperTreeUpdater;

public class NoLinearTreeUpdater<
        TAction extends Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
        implements TreeUpdater<TAction, TPlayerObservation, TOpponentObservation, NoLinearMetadata<TAction>, TState> {

    private static final Logger logger = LoggerFactory.getLogger(PaperTreeUpdater.class);

    private final int riskLevelCount;

    public NoLinearTreeUpdater(int riskLevelCount) {
        this.riskLevelCount = riskLevelCount;
    }

    @Override
    public void updateTree(SearchNode<TAction, TPlayerObservation, TOpponentObservation, NoLinearMetadata<TAction>, TState> expandedNode) {
        int i = 0;

        expandedNode.getSearchNodeMetadata().increaseVisitCounter();


        expandedNode = expandedNode.getParent();
        while (!expandedNode.isRoot()) {
            updateNode(expandedNode);
            expandedNode = expandedNode.getParent();
            i++;
        }
        updateNode(expandedNode);
        logger.trace("Traversing updated traversed [{}] tree levels", i);
    }

    private void updateNode(SearchNode<TAction, TPlayerObservation, TOpponentObservation, NoLinearMetadata<TAction>, TState> node) {
        var searchNodeMetadata = node.getSearchNodeMetadata();
        searchNodeMetadata.increaseVisitCounter();

        var childrenMap = node.getChildNodeMap();

        // todo

    }
}
