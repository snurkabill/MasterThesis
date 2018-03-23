package vahy.environment.state;

public class RewardStateReturn {

    private final double reward;
    private final IState state;

    public RewardStateReturn(double reward, IState state) {
        this.reward = reward;
        this.state = state;
    }

    public double getReward() {
        return reward;
    }

    public IState getState() {
        return state;
    }
}
