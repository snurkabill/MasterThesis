package vahy.riskBasedSearch;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperNodeSelector;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class RiskBasedSelector<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends PaperNodeSelector<TAction, TPlayerObservation, TOpponentObservation, TState> {

    private final Logger logger = LoggerFactory.getLogger(RiskBasedSelector.class.getName());

    private final double totalRiskAllowed;

    public RiskBasedSelector(double cpuctParameter, SplittableRandom random, double totalRiskAllowed) {
        super(cpuctParameter, random);
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
        if(node.isPlayerTurn()) {
            int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();

            double minimalRisk = node
                .getChildNodeStream()
                .mapToDouble(x -> x.getSearchNodeMetadata().getPredictedRisk())
                .min()
                .orElseThrow(() -> new IllegalStateException("Minimal risk does not exist"));

            if(minimalRisk <= totalRiskAllowed) {

                ImmutableTuple<Double, Double> minMax = getMinMax(node);
                final double min = minMax.getFirst();
                final double max = minMax.getSecond();
                assert(min <= max); // paranoia

                List<ImmutableTuple<TAction, Double>> actionsUcbValue = node.getChildNodeStream()
                    .map(getSearchNodeImmutableTupleFunction(totalNodeVisitCount, min, max))
                    .collect(Collectors.toList());

                CLP model = new CLP();
                final CLPExpression totalRiskExpression = model.createExpression();
                final CLPExpression sumToOneExpression = model.createExpression();
                final SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> finalNodeReference = node;
                List<ImmutableTuple<ImmutableTuple<TAction, Double>, CLPVariable>> collect = actionsUcbValue
                    .stream()
                    .map(x -> {
                        CLPVariable probabilityVariable = model.addVariable().lb(0.0).ub(1.0);
                        model.setObjectiveCoefficient(probabilityVariable, x.getSecond());
                        totalRiskExpression.add(probabilityVariable,
                            finalNodeReference.getChildNodeMap().get(x.getFirst()).getSearchNodeMetadata().getPredictedRisk());
                        sumToOneExpression.add(probabilityVariable, 1.0);
                        return new ImmutableTuple<>(x, probabilityVariable);
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
                totalRiskExpression.leq(totalRiskAllowed);
                sumToOneExpression.eq(1.0);

                CLP.STATUS status = model.maximize();

                if(status != CLP.STATUS.OPTIMAL) {
                    throw new IllegalStateException("Optimal solution was not found");
                }

                ArrayList<Double> probabilities = collect.stream().map(x -> x.getSecond().getSolution()).collect(Collectors.toCollection(ArrayList::new));
                int actionIndex = RandomDistributionUtils.getRandomIndexFromDistribution(probabilities, random);

                return collect.get(actionIndex).getFirst().getFirst();
            } else {
                return super.getBestAction(node);
            }
        } else {
            return sampleOpponentAction(node);
        }
    }
}
