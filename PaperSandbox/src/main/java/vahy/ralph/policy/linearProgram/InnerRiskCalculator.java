package vahy.ralph.policy.linearProgram;

import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.ralph.metadata.RalphMetadata;

import java.util.SplittableRandom;

public class InnerRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState> {

    public InnerRiskCalculator(boolean maximize, SplittableRandom random, NoiseStrategy strategy) {
        super(maximize, random, strategy);
    }

    @Override
    protected void setLeafObjective(InnerElement<TAction, TObservation, TSearchNodeMetadata, TState> element) {
        var node = element.getNode();
        var inGameEntityId = node.getStateWrapper().getInGameEntityId();
        if(((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit()) {
            element.getFlowWithCoefficient().setCoefficient(element.getFlowWithCoefficient().getCoefficient() + (1.0 * element.getModifier()));
        } else {
            element.getFlowWithCoefficient().setCoefficient(element.getFlowWithCoefficient().getCoefficient() + (node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId] * element.getModifier()));
        }
    }

//            @Override
//            protected void setLeafObjectiveWithFlow(List<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> searchNodes, CLPVariable parentFlow) {
//                double sum = 0.0;
//                for (var entry : searchNodes) {
//                    var inGameEntityId = entry.getStateWrapper().getInGameEntityId();
//                    var metadata = entry.getSearchNodeMetadata();
//                    sum += (((PaperStateWrapper<TAction, TObservation, TState>)entry.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId]) * metadata.getPriorProbability();
//                }
//                model.setObjectiveCoefficient(parentFlow, sum);
//            }

    @Override
    protected void finalizeHardConstraints() {
        // this is it
    }
}
