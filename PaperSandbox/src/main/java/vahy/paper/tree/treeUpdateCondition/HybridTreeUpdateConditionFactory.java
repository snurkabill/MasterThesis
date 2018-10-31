package vahy.paper.tree.treeUpdateCondition;

public class HybridTreeUpdateConditionFactory implements TreeUpdateConditionFactory {

    private final long firstStepMillisecondBound;
    private final int firstStepUpdateCount;
    private final long otherStepMillisecondBound;

    public HybridTreeUpdateConditionFactory(long firstStepMillisecondBound, int firstStepUpdateCount, long otherStepMillisecondBound) {
        this.firstStepMillisecondBound = firstStepMillisecondBound;
        this.firstStepUpdateCount = firstStepUpdateCount;
        this.otherStepMillisecondBound = otherStepMillisecondBound;
    }

    @Override
    public TreeUpdateCondition create() {
        return new TreeUpdateConditionImpl(firstStepMillisecondBound, firstStepUpdateCount, otherStepMillisecondBound);
    }
}
