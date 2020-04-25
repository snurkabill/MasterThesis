package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.function.Supplier;

public interface DataPointGenerator extends Supplier<ImmutableTuple<Double, List<Double>>> {

    String getDataTitle();

//    void addDataPoint(Double x, Double y);

}
