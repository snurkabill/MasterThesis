package vahy.api.learning;

import java.util.function.IntSupplier;

public abstract class AbstractTrainer {

    public abstract void trainPolicy(IntSupplier episodeCount);

}
