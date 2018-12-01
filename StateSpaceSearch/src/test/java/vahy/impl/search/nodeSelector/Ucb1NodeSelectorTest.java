package vahy.impl.search.nodeSelector;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.MCTS.ucb1.Ucb1NodeSelector;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;
import vahy.testDomain.search.TestHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.SplittableRandom;

public class Ucb1NodeSelectorTest {

    @Test
    public void testUcb1SelectPlayerAction() {
        SplittableRandom random = new SplittableRandom(0);
        Ucb1NodeSelector<TestAction, DoubleReward, DoubleVector, TestState> nodeSelector = new Ucb1NodeSelector<>(random, 1.0);
        nodeSelector.setNewRoot(TestHelper.createOneLevelTree(true));
        SearchNode<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> selectedNode = nodeSelector.selectNextNode();
        Assert.assertEquals(selectedNode.getAppliedAction(), Arrays.stream(TestAction.playerActions).max(Comparator.comparingDouble(TestAction::getReward)).get());
    }

    @Test
    public void testUcb1SelectOpponentAction() {
        SplittableRandom random = new SplittableRandom(0);
        Ucb1NodeSelector<TestAction, DoubleReward, DoubleVector, TestState> nodeSelector = new Ucb1NodeSelector<>(random, 1.0);
        nodeSelector.setNewRoot(TestHelper.createOneLevelTree(false));
        SearchNode<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> selectedNode = nodeSelector.selectNextNode();
        Assert.assertEquals(selectedNode.getAppliedAction(), Arrays.stream(TestAction.opponentActions).min(Comparator.comparingDouble(TestAction::getReward)).get());
    }
}
