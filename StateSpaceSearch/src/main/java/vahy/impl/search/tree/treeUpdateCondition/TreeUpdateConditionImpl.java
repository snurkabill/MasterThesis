package vahy.impl.search.tree.treeUpdateCondition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;

public class TreeUpdateConditionImpl implements TreeUpdateCondition {

    private final Logger logger = LoggerFactory.getLogger(TreeUpdateConditionImpl.class);

    private final long firstStepTimeBoundInMilliseconds;
    private final long otherStepTimeBoundInMilliseconds;
    private final int firstStepTreeUpdatesBound;

    private boolean wasFirstStepDone = false;

    private long stepStart;
    private int callCount;

    public TreeUpdateConditionImpl(long firstStepTimeBoundInMilliseconds, int firstStepTreeUpdatesBound, long otherStepTimeBoundInMilliseconds) {
        this.firstStepTimeBoundInMilliseconds = firstStepTimeBoundInMilliseconds;
        this.firstStepTreeUpdatesBound = firstStepTreeUpdatesBound;
        this.otherStepTimeBoundInMilliseconds = otherStepTimeBoundInMilliseconds;
    }

    @Override
    public void treeUpdateRequired() {
        stepStart = System.currentTimeMillis();
        callCount = 0;
    }

    @Override
    public boolean isConditionSatisfied() {
        callCount++;
        long stepDurationSoFar = System.currentTimeMillis() - stepStart;

        if(!wasFirstStepDone) {
            if(stepDurationSoFar > firstStepTimeBoundInMilliseconds) {
                logger.info("First step done since time limit [{}]ms was hit. Done updates: [{}]", firstStepTimeBoundInMilliseconds, callCount);
                return false;
            }
            if(callCount > firstStepTreeUpdatesBound) {
                logger.info("First step done since call count [{}] was hit. In [{}]ms", callCount, stepDurationSoFar);
                return false;
            }
            return true;
        } else {
            return stepDurationSoFar <= otherStepTimeBoundInMilliseconds;
        }
    }

    @Override
    public void treeUpdateFinished() {
        wasFirstStepDone = true;
        // empty
    }

    @Override
    public void reset() {
        wasFirstStepDone = false;

    }
}
