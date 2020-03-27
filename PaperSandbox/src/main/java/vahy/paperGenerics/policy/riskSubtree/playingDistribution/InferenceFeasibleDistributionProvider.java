package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class InferenceFeasibleDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier;

    public InferenceFeasibleDistributionProvider(Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier) {
        super(true);
        this.subtreeRiskCalculatorSupplier = subtreeRiskCalculatorSupplier;
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
        double temperature,
        SplittableRandom random,
        double totalRiskAllowed)
    {
        int childCount = node.getChildNodeMap().size();
        List<TAction> actionList = new ArrayList<>(childCount);
        double[] distributionArray = new double[childCount];
        double[] riskArray = new double[childCount];

        int j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
            actionList.add(entry.getKey());
            var metadata = entry.getValue().getSearchNodeMetadata();
            distributionArray[j] = metadata.getNodeProbabilityFlow().getSolution();
            riskArray[j] = distributionArray[j] - TOLERANCE <= 0.0 ? 1.0 : subtreeRiskCalculatorSupplier.get().calculateRisk(entry.getValue()) / distributionArray[j];
            j++;
        }

        RandomDistributionUtils.tryToRoundDistribution(distributionArray);
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(distributionArray, random);
        return new PlayingDistribution<>(actionList.get(index), index, distributionArray, riskArray, actionList, subtreeRiskCalculatorSupplier);

    }
}
