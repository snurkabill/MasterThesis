package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.util.function.Supplier;

public interface DataPointGenerator extends Supplier<ImmutableTuple<Double, Double>> {

    String getDataTitle();

}
