package vahy.impl.search.tree.treeUpdateCondition;

import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;

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
