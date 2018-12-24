package vahy.paperGenerics.benchmark;


import vahy.api.model.Action;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicySupplier;

public class PaperBenchmarkingPolicy<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>> {

    private final String policyName;
    private final PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> policySupplier;

    public PaperBenchmarkingPolicy(String policyName, PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> policySupplier) {
        this.policyName = policyName;
        this.policySupplier = policySupplier;
    }

    public String getPolicyName() {
        return policyName;
    }

    public PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> getPolicySupplier() {
        return policySupplier;
    }
}
