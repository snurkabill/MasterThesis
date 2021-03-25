package vahy.ralph.policy.linearProgram;

import com.quantego.clp.CLPVariable;

public class FlowWithCoefficient {

    protected final CLPVariable closestParentFlow;
    protected double coefficient;

    public FlowWithCoefficient(CLPVariable closestParentFlow) {
        this.closestParentFlow = closestParentFlow;
    }

    public CLPVariable getClosestParentFlow() {
        return closestParentFlow;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }
}
