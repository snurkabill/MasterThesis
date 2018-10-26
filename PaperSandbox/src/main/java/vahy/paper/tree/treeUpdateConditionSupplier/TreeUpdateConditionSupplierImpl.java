package vahy.paper.tree.treeUpdateConditionSupplier;

public class TreeUpdateConditionSupplierImpl implements TreeUpdateConditionSupplier {

    private final long firstStepTimeBoundInMilliseconds;
    private final long otherStepTimeBOundInMilliseconds;
    private final int firstStepTreeUpdatesBound;

    private boolean wasFirstStepDone = false;

    private long stepStart;
    private int callCount;

    public TreeUpdateConditionSupplierImpl(long firstStepTimeBoundInMilliseconds, int firstStepTreeUpdatesBound, long otherStepTimeBOundInMilliseconds) {
        this.firstStepTimeBoundInMilliseconds = firstStepTimeBoundInMilliseconds;
        this.firstStepTreeUpdatesBound = firstStepTreeUpdatesBound;
        this.otherStepTimeBOundInMilliseconds = otherStepTimeBOundInMilliseconds;
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
                return false;
            }
            if(callCount > firstStepTreeUpdatesBound) {
                return false;
            }
            wasFirstStepDone = true;
            return true;
        } else {
            return stepDurationSoFar <= otherStepTimeBOundInMilliseconds;
        }
    }

    @Override
    public void treeUpdateFinished() {
        // empty
    }
}
