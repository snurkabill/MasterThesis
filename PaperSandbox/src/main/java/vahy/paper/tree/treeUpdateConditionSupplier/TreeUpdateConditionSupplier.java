package vahy.paper.tree.treeUpdateConditionSupplier;

public interface TreeUpdateConditionSupplier {

    void treeUpdateRequired();

    boolean isConditionSatisfied();

    void treeUpdateFinished();

}
