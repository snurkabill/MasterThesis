package vahy.ralph.policy.linearProgram;

import com.quantego.clp.CLPVariable;

public class FlowWithCoefficient {

    private final CLPVariable closestParentFlow;
    private double coefficient;

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
