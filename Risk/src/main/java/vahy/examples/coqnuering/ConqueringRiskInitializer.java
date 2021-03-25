package vahy.examples.coqnuering;

import vahy.api.policy.PolicyMode;
import vahy.examples.conquering.ConqueringAction;
import vahy.examples.conquering.ConqueringConfig;
import vahy.examples.conquering.ConqueringState;
import vahy.examples.conquering.ConqueringStaticPart;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.SplittableRandom;

public class ConqueringRiskInitializer extends AbstractInitialStateSupplier<ConqueringConfig, ConqueringAction, DoubleVector, ConqueringRiskState> {

    public ConqueringRiskInitializer(ConqueringConfig problemConfig, SplittableRandom random) {
        super(problemConfig, random);
    }

    @Override
    protected ConqueringRiskState createState_inner(ConqueringConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        var staticPart = new ConqueringStaticPart(problemConfig.getHallwayLength(), problemConfig.getPlayerCount() + 1, problemConfig.getStepPenalty(), problemConfig.getWinReward(), problemConfig.getKillProbability(), problemConfig.getMaximalStepCountBound());
        var playerPositionArray = random.ints(problemConfig.getPlayerCount(), 1, problemConfig.getHallwayLength()).toArray();
        var playerPositionArray_hidden = Arrays.copyOf(playerPositionArray, playerPositionArray.length);
        var isInGameArray = new boolean[problemConfig.getPlayerCount()];
        var isEliminatedArray = new boolean[problemConfig.getPlayerCount() + 1];
        Arrays.fill(isInGameArray, true);
        return new ConqueringRiskState(new ConqueringState(staticPart, playerPositionArray, playerPositionArray_hidden, isInGameArray, problemConfig.getPlayerCount(), 1, 0, isEliminatedArray));
    }
}
