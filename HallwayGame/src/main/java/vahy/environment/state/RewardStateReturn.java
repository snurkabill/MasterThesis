package vahy.environment.state;

public class RewardStateReturn {

    // exists because generics are fakin slow and we are going to create lot of these.

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
