package vahy.api.experiment;

import java.util.function.Supplier;

public interface ParameterSupplier<ParameterType> extends Supplier<ParameterType>  {

    String toLog();

    String toFile();

}
