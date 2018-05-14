package vahy.impl.search.nodeSelector.treeTraversing;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.utils.StreamUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SplittableRandom;

public class Ucb1EstimateNormalizingNodeSelector<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation>>
    extends Ucb1NodeSelector<TAction, TReward, TObservation, TState>{

    public Ucb1EstimateNormalizingNodeSelector(SplittableRandom random, double weight) {
        super(random, weight);
    }

    @Override
    public SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> selectNextNode() {
        checkRoot();
        SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> node = this.root;
        while(!node.isLeaf()) {
            Ucb1SearchNodeMetadata<TAction, TReward> nodeMetadata = node.getSearchNodeMetadata();
            SearchNode<TAction, TReward, TObservation, Ucb1StateActionMetadata<TReward>, Ucb1SearchNodeMetadata<TAction, TReward>, TState> finalNode = node;
            double softmaxDenominator = node
                .getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .mapToDouble(x -> Math.exp(x.getValue().getEstimatedTotalReward().getValue()))
                .sum();

            TAction bestAction = node.getSearchNodeMetadata()
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .collect(StreamUtils.toRandomizedMaxCollector(
                    Comparator.comparing(
                        o -> calculateUCBValue(
                            Math.exp(finalNode.getChildNodeMap().get(o.getKey()).getSearchNodeMetadata().getEstimatedTotalReward().getValue()) / softmaxDenominator,
                            nodeMetadata.getVisitCounter(),
                            o.getValue().getVisitCounter())),
                    random))
                .getKey();


            Set<Map.Entry<TAction, Ucb1StateActionMetadata<TReward>>> entries = node.getSearchNodeMetadata().getStateActionMetadataMap().entrySet();
            List<Double> normalizedEstimates = new ArrayList<>();
            List<Double> ucbValues = new ArrayList<>();
            List<TAction> actions = new ArrayList<>();
            List<Integer> visits = new ArrayList<>();
            List<Double> estimates = new ArrayList<>();

            for (Map.Entry<TAction, Ucb1StateActionMetadata<TReward>> entry : entries) {
                actions.add(entry.getKey());
                normalizedEstimates.add(Math.exp(entry.getValue().getEstimatedTotalReward().getValue()) / softmaxDenominator);
                ucbValues.add(calculateUCBValue(Math.exp(entry.getValue().getEstimatedTotalReward().getValue()) / softmaxDenominator, finalNode.getSearchNodeMetadata().getVisitCounter(), entry.getValue().getVisitCounter()));
                visits.add(entry.getValue().getVisitCounter());
                estimates.add(entry.getValue().getEstimatedTotalReward().getValue());
            }

            nodeMetadata.increaseVisitCounter();
            nodeMetadata.getStateActionMetadataMap().get(bestAction).increaseVisitCounter();
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }

}
