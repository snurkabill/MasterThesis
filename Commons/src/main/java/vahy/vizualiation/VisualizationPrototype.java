package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VisualizationPrototype {

    public static void main(String[] args) throws InterruptedException {

        MyShittyFrameVisualization myShittyFrameVisualization = new MyShittyFrameVisualization("TestWindow", List.of("OneTitle", "SecondTitle"), List.of("Xlabel", "XLabel2"), List.of("Ylabel", "YLabel2"), Color.RED);

        var titles = List.of("First", "Second");

        var data = new ArrayList<List<ImmutableTuple<Double, Double>>>();
        data.add(List.of(new ImmutableTuple<>(0.0, 1.0), new ImmutableTuple<>(1.0, 2.0), new ImmutableTuple<>(2.0, 3.0)));
        data.add(List.of(new ImmutableTuple<>(0.0, 1.0), new ImmutableTuple<>(1.0, 4.0), new ImmutableTuple<>(2.0, 9.0)));

        var data2 = new ArrayList<List<ImmutableTuple<Double, Double>>>();
        data2.add(List.of(new ImmutableTuple<>(0.0, 1.0), new ImmutableTuple<>(1.0, 2.0), new ImmutableTuple<>(2.0, 3.0)));
        data2.add(List.of(new ImmutableTuple<>(0.0, 1.0), new ImmutableTuple<>(1.0, 4.0), new ImmutableTuple<>(2.0, 9.0)));

        var dataseries = XYDatasetBuilder.createDataset(data, titles);
        var dataseries2 = XYDatasetBuilder.createDataset(data2, titles);

        myShittyFrameVisualization.draw(List.of(dataseries, dataseries2));

        Thread.sleep(100000);

        System.out.println("asdf");


    }

}
