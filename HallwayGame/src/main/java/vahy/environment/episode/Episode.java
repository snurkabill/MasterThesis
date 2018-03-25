package vahy.environment.episode;

import vahy.environment.ActionType;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.IState;
import vahy.environment.state.RewardStateReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Episode {

    private final IState initialState;
    private final IOneHotPolicy policy;
    private List<Double> rewardHistory = new ArrayList<>();

    public Episode(IState initialState, IOneHotPolicy policy) {
        this.initialState = initialState;
        this.policy = policy;
    }

    public void runEpisode() {
        IState state = initialState;
        while(!state.isFinalState()) {
            System.out.println(Arrays.toString(state.getFeatureVector()));
            ActionType action = policy.getDiscreteAction(state);
            RewardStateReturn rewardStateReturn = state.applyAction(action);
            state = rewardStateReturn.getState();
            rewardHistory.add(rewardStateReturn.getReward());
        }
    }

    public List<Double> getRewardHistory() {
        return rewardHistory;
    }

    public double getTotalEpisodicReward() {
        return rewardHistory.stream().reduce(0.0, (x, y) -> x + y);
    }
}
