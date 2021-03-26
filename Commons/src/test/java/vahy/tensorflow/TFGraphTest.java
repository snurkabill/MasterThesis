package vahy.tensorflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat64;

public class TFGraphTest {

    @Test
    public void addTest() {

        Graph graph = new Graph();
        Operation a = graph.opBuilder("Const", "a")
            .setAttr("dtype", DataType.DT_DOUBLE)
            .setAttr("value", TFloat64.scalarOf(3.0))
            .build();
        Operation b = graph.opBuilder("Const", "b")
            .setAttr("dtype", DataType.DT_DOUBLE)
            .setAttr("value", TFloat64.scalarOf(2.0))
            .build();


        Operation x = graph.opBuilder("Placeholder", "x")
            .setAttr("dtype", DataType.DT_DOUBLE)
            .build();
        Operation y = graph.opBuilder("Placeholder", "y")
            .setAttr("dtype", DataType.DT_DOUBLE)
            .build();

        Operation ax = graph.opBuilder("Mul", "ax")
            .addInput(a.<TFloat64>output(0))
            .addInput(x.<TFloat64>output(0))
            .build();

        Operation by = graph.opBuilder("Mul", "by")
            .addInput(b.<TFloat64>output(0))
            .addInput(y.<TFloat64>output(0))
            .build();

        Operation z = graph.opBuilder("Add", "z")
            .addInput(ax.<TFloat64>output(0))
            .addInput(by.<TFloat64>output(0))
            .build();

        Session sess = new Session(graph);

        TFloat64 tensor = (TFloat64) sess.runner().fetch(z.<TFloat64>output(0))
            .feed(x.<TFloat64>output(0), TFloat64.scalarOf(3.0))
            .feed(y.<TFloat64>output(0), TFloat64.scalarOf(6.0))
            .run().get(0);

        Assertions.assertEquals(21.0, tensor.asRawTensor().data().asDoubles().getDouble(0));
    }


}
