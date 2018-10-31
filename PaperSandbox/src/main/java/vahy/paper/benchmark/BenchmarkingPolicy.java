package vahy.paper.benchmark;

import vahy.paper.policy.PaperPolicySupplier;

public class BenchmarkingPolicy {

    private final String policyName;
    private final PaperPolicySupplier policySupplier;

    public BenchmarkingPolicy(String policyName, PaperPolicySupplier policySupplier) {
        this.policyName = policyName;
        this.policySupplier = policySupplier;
    }

    public String getPolicyName() {
        return policyName;
    }

    public PaperPolicySupplier getPolicySupplier() {
        return policySupplier;
    }
}
