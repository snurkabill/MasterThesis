package vahy.impl.search.MCTS.ucb1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.SplittableRandom;

public class Ucb1NodeSelector<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> {

    private final Logger logger = LoggerFactory.getLogger(Ucb1NodeSelector.class);

    protected final SplittableRandom random;
    protected final double explorationConstant; // TODO: get rid of explorationConstant here. There are Ucb1 heuristics with changing exploration constant

    public Ucb1NodeSelector(SplittableRandom random, double explorationConstant) {
        this.random = random;
        this.explorationConstant = explorationConstant;
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            int nodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
            var isPlayerTurn = node.isPlayerTurn();
            var action = node.getChildNodeStream()
                .collect(StreamUtils.toRandomizedMaxCollector(
                    Comparator.comparing(
                        o -> calculateUCBValue( // TODO: optimize so calls are done only once
                            (isPlayerTurn ? 1.0 : -1.0) * o.getSearchNodeMetadata().getExpectedReward(),
                            explorationConstant,
                            nodeVisitCount,
                            o.getSearchNodeMetadata().getVisitCounter())),
                    random))
                .getAppliedAction();
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

//    @Override
//    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, MonteCarloTreeSearchMetadata, TState> node) {
//        int nodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
//        return node.getChildNodeStream()
//            .collect(StreamUtils.toRandomizedMaxCollector(
//                Comparator.comparing(
//                    o -> calculateUCBValue( // TODO: optimize so calls are done only once
//                        (node.isPlayerTurn() ? 1.0 : -1.0) * o.getSearchNodeMetadata().getExpectedReward(),
//                        explorationConstant,
//                        nodeVisitCount,
//                        o.getSearchNodeMetadata().getVisitCounter())),
//                random))
//            .getAppliedAction();
//    }

    protected double calculateUCBValue(double estimatedValue, double explorationConstant, int parentVisitCount, int actionVisitCount) {
        return estimatedValue + explorationConstant * Math.sqrt(Math.log(parentVisitCount) / (1.0 + actionVisitCount));

    }
}
