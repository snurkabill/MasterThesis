package vahy.paper.benchmark;

import vahy.paper.policy.PolicySupplier;

public class BenchmarkingPolicy {

    private final String policyName;
    private final PolicySupplier policySupplier;

    public BenchmarkingPolicy(String policyName, PolicySupplier policySupplier) {
        this.policyName = policyName;
        this.policySupplier = policySupplier;
    }

    public String getPolicyName() {
        return policyName;
    }

    public PolicySupplier getPolicySupplier() {
        return policySupplier;
    }
}
