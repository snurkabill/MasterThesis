package vahy.impl.search.nodeSelector;

public class Ucb1NodeSelectorTest {

//    @Test
//    public void testUcb1SelectPlayerAction() {
//        SplittableRandom random = new SplittableRandom(0);
//        Ucb1NodeSelector<TestAction, DoubleVector, TestState, TestState> nodeSelector = new Ucb1NodeSelector<>(random, 1.0);
//        nodeSelector.setNewRoot(TestHelper.createOneLevelTree(true));
//        SearchNode<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> selectedNode = nodeSelector.selectNextNode();
//        Assert.assertEquals(selectedNode.getAppliedAction(), Arrays.stream(TestAction.playerActions).max(Comparator.comparingDouble(TestAction::getReward)).get());
//    }
//
//    @Test
//    public void testUcb1SelectOpponentAction() {
//        SplittableRandom random = new SplittableRandom(0);
//        Ucb1NodeSelector<TestAction, DoubleVector, TestState, TestState> nodeSelector = new Ucb1NodeSelector<>(random, 1.0);
//        nodeSelector.setNewRoot(TestHelper.createOneLevelTree(false));
//        nodeSelector.selectNextNode();
//        SearchNode<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> selectedNode = nodeSelector.selectNextNode();
//        Assert.assertEquals(selectedNode.getAppliedAction(), Arrays.stream(TestAction.opponentActions).min(Comparator.comparingDouble(TestAction::getReward)).get());
//    }
}
