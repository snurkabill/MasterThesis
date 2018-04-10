package vahy.environment.episode;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.chart.ChartBuilder;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Episode {

    private final ImmutableStateImpl initialState;
    private final IOneHotPolicy playerPolicy;

    public Episode(ImmutableStateImpl initialState, IOneHotPolicy playerPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
    }

    public void runEpisode() {
        ImmutableStateImpl state = this.initialState;
        List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> stepHistory = new LinkedList<>();
        while(!state.isFinalState()) {
            ActionType action = playerPolicy.getDiscreteAction(state);
            System.out.println("Action: [" + action + "]");
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = state.applyAction(action);
            state = (ImmutableStateImpl) stateRewardReturn.getState();
            stepHistory.add(stateRewardReturn);
            if(!state.isFinalState()) {
                stateRewardReturn =  state.applyEnvironmentAction();
                stepHistory.add(stateRewardReturn);
                state = (ImmutableStateImpl) stateRewardReturn.getState();
            }
        }

        List<Double> rewardHistory = stepHistory.stream().map(x -> x.getReward().getValue()).collect(Collectors.toList());
        LinkedList<Double> runningSum = new LinkedList<>();
        for (Double value : rewardHistory) {
            if(runningSum.isEmpty()) {
                runningSum.add(value);
            } else {
                runningSum.add(runningSum.getLast() + value);
            }
        }
        List<List<Double>> datasets = new ArrayList<>();
        datasets.add(rewardHistory);
        datasets.add(runningSum);
        ChartBuilder.chart(new File("asdf"), datasets, "datasets");
    }

}
