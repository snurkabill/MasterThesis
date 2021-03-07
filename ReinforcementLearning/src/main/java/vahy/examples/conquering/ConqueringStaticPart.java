package vahy.examples.conquering;

import java.util.Arrays;
import java.util.EnumMap;

public class ConqueringStaticPart {

    private final int lengthOfHall;
    private final double[] noRewardArray;
    private final double[][] actionReward;

    private final int totalEntityCount;

    private final int defaultStepPenalty;
    private final int rewardPerWinning;
    private final double baseKillProbability;

    private final int totalStepsAllowed;

    private final EnumMap<ConqueringAction, ConqueringAction[]> observedActionMap;


    public ConqueringStaticPart(int lengthOfHall, int totalEntityCount, int defaultStepPenalty, int rewardPerWinning, double baseKillProbability, int totalStepsAllowed) {
        this.lengthOfHall = lengthOfHall;
        this.totalEntityCount = totalEntityCount;
        this.defaultStepPenalty = defaultStepPenalty;
        this.rewardPerWinning = rewardPerWinning;
        this.baseKillProbability = baseKillProbability;
        this.totalStepsAllowed = totalStepsAllowed;

        this.observedActionMap = new EnumMap<ConqueringAction, ConqueringAction[]>(ConqueringAction.class);
        for (ConqueringAction value : ConqueringAction.values()) {
            var array = new ConqueringAction[totalEntityCount];
            Arrays.fill(array, value);
            observedActionMap.put(value, array);
        }

        this.noRewardArray = new double[totalEntityCount];
        this.actionReward = new double[totalEntityCount][];
        for (int i = 0; i < actionReward.length; i++) {
            actionReward[i] = new double[totalEntityCount];
            actionReward[i][i] = -defaultStepPenalty;
        }
    }

    public int getLengthOfHall() {
        return lengthOfHall;
    }

    public double[] getActionReward(int inGameEntityId) {
        return actionReward[inGameEntityId];
    }

    public int getTotalEntityCount() {
        return totalEntityCount;
    }

    public int getDefaultStepPenalty() {
        return defaultStepPenalty;
    }

    public int getRewardPerWinning() {
        return rewardPerWinning;
    }

    public double getBaseKillProbability() {
        return baseKillProbability;
    }

    public int getTotalStepsAllowed() {
        return totalStepsAllowed;
    }

    public ConqueringAction[] getObservedActionArray(ConqueringAction action) {
        return observedActionMap.get(action);
    }
}
