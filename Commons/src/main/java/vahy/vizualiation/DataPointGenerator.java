package vahy.vizualiation;

import java.util.function.Supplier;

public interface DataPointGenerator extends Supplier<DataSample> {

    String getDataTitle();

//    void addDataPoint(Double x, Double y);

}
