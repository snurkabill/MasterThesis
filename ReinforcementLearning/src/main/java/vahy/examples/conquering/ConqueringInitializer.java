package vahy.examples.conquering;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.SplittableRandom;

public class ConqueringInitializer extends AbstractInitialStateSupplier<ConqueringConfig, ConqueringAction, DoubleVector, ConqueringState> {

    protected ConqueringInitializer(ConqueringConfig problemConfig, SplittableRandom random) {
        super(problemConfig, random);
    }

    @Override
    protected ConqueringState createState_inner(ConqueringConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        var staticPart = new ConqueringStaticPart(problemConfig.getHallwayLength(), problemConfig.getPlayerCount() + 1, problemConfig.getStepPenalty(), problemConfig.getWinReward(), problemConfig.getKillProbability(), problemConfig.getMaximalStepCountBound());
        var playerPositionArray = random.ints(problemConfig.getPlayerCount(), 1, problemConfig.getHallwayLength()).toArray();
        var playerPositionArray_hidden = Arrays.copyOf(playerPositionArray, playerPositionArray.length);
        var isInGameArray = new boolean[problemConfig.getPlayerCount()];
        Arrays.fill(isInGameArray, true);
        return new ConqueringState(staticPart, playerPositionArray, playerPositionArray_hidden, isInGameArray, problemConfig.getPlayerCount(), 1, 0);
    }
}
