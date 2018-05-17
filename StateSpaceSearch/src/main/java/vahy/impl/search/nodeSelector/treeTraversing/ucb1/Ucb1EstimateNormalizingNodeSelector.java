package vahy.impl.search.nodeSelector.treeTraversing.ucb1;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
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

            double min = nodeMetadata
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .mapToDouble(x -> x.getValue().getEstimatedTotalReward().getValue())
                .min()
                .orElseThrow(() -> new IllegalArgumentException("Minimal element was not found"));

            double max = nodeMetadata
                .getStateActionMetadataMap()
                .entrySet()
                .stream()
                .mapToDouble(x -> x.getValue().getEstimatedTotalReward().getValue())
                .max()
                .orElseThrow(() -> new IllegalArgumentException("Maximal element was not found"));

            double explorationConstant = (max + min) / 2.0;
            LinkedList<ImmutableTuple<TAction, Double>> valuedActions = new LinkedList<>();

            for (Map.Entry<TAction, Ucb1StateActionMetadata<TReward>> entry : nodeMetadata.getStateActionMetadataMap().entrySet()) {
                valuedActions.add(new ImmutableTuple<>(entry.getKey(), calculateUcb1ValueWithExplorationConstant(
                    entry.getValue().getEstimatedTotalReward().getValue(),
                    explorationConstant,
                    nodeMetadata.getVisitCounter(),
                    entry.getValue().getVisitCounter())));
            }

            TAction bestAction = valuedActions
                .stream()
                .collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random))
                .getFirst();

//            TAction bestAction2 = nodeMetadata
//                .getStateActionMetadataMap()
//                .entrySet()
//                .stream()
//                .collect(
//                    StreamUtils.toRandomizedMaxCollector(Comparator.comparing(
//                        x -> ))
//                        , random))
//                .getKey();

//            double softmaxDenominator = node
//                .getSearchNodeMetadata()
//                .getStateActionMetadataMap()
//                .entrySet()
//                .stream()
//                .mapToDouble(x -> Math.exp(x.getValue().getEstimatedTotalReward().getValue()))
//                .sum();
//
//            Set<Map.Entry<TAction, Ucb1StateActionMetadata<TReward>>> entries = node.getSearchNodeMetadata().getStateActionMetadataMap().entrySet();
//            List<Double> normalizedEstimates = new ArrayList<>();
//            List<Double> ucbValues = new ArrayList<>();
//            List<TAction> actions = new ArrayList<>();
//            List<Integer> visits = new ArrayList<>();
//            List<Double> estimates = new ArrayList<>();
//
//            List<ImmutableTuple<TAction, Double>> actionValueList = new ArrayList<>();
//
//            for (Map.Entry<TAction, Ucb1StateActionMetadata<TReward>> entry : entries) {
//                actions.add(entry.getKey());
//                normalizedEstimates.add(Math.exp(entry.getValue().getEstimatedTotalReward().getValue()) / softmaxDenominator);
//                ucbValues.add(calculateUCBValue(Math.exp(entry.getValue().getEstimatedTotalReward().getValue()) / softmaxDenominator, finalNode.getSearchNodeMetadata().getVisitCounter(), entry.getValue().getVisitCounter()));
//                visits.add(entry.getValue().getVisitCounter());
//                estimates.add(entry.getValue().getEstimatedTotalReward().getValue());
//
//                actionValueList.add(new ImmutableTuple<>(entry.getKey(), calculateUCBValue(1 / ( softmaxDenominator - Math.exp(entry.getValue().getEstimatedTotalReward().getValue())) , finalNode.getSearchNodeMetadata().getVisitCounter(), entry.getValue().getVisitCounter())));
//            }
//
////            TAction bestAction = node.getSearchNodeMetadata()
////                .getStateActionMetadataMap()
////                .entrySet()
////                .stream()
////                .collect(StreamUtils.toRandomizedMaxCollector(
////                    Comparator.comparing(
////                        o -> calculateUCBValue(
////                            Math.exp(finalNode.getChildNodeMap().get(o.getKey()).getSearchNodeMetadata().getEstimatedTotalReward().getValue()) / softmaxDenominator,
////                            nodeMetadata.getVisitCounter(),
////                            o.getValue().getVisitCounter())),
////                    random))
////                .getKey();
//
//            TAction bestAction = actionValueList.stream().collect(StreamUtils.toRandomizedMaxCollector(Comparator.comparing(ImmutableTuple::getSecond), random)).getFirst();
//
//
            nodeMetadata.increaseVisitCounter();
            nodeMetadata.getStateActionMetadataMap().get(bestAction).increaseVisitCounter();
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }

    private double calculateUcb1ValueWithExplorationConstant(double estimatedValue, double explorationConstant, int parentVisitCount, int childVisitCount) {
        return estimatedValue + explorationConstant * Math.sqrt(Math.log(parentVisitCount) / childVisitCount);
    }

}
