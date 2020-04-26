package vahy.thirdpartyprototype.TF;

import vahy.paperGenerics.reinforcement.learning.tf.TFModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.SplittableRandom;

public class TestingTF2 {

    public static void main(String[] args) {

        double[] vector1 = new double[]  {0.5, -0.5, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[] vector2 = new double[]  {-0.5, -0.5, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[] vector3 = new double[]  {-0.5, -0.5, 1.0, 0.0, 0.0, 0.0, 1.0};
        double[] vector4 = new double[]  {-0.5, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        double[] vector5 = new double[]  {-0.5, 0.5, 1.0, 0.0, 0.0, 0.0, 1.0};
        double[] vector6 = new double[]  {-0.5, 0.5, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[] vector7 = new double[]  {-0.5, 0.5, 1.0, 0.0, 1.0, 0.0, 0.0};
        double[] vector8 = new double[]  {-0.5, 0.5, 1.0, 0.0, 0.0, 1.0, 0.0};
        double[] vector9 = new double[]  {0.5, -0.5, 1.0, 0.0, 0.0, 0.0, 1.0};
        double[] vector10 = new double[] {0.5, -0.5, 1.0, 0.0, 1.0, 0.0, 0.0};
        double[] vector11 = new double[] {0.5, -0.5, 1.0, 0.0, 0.0, 1.0, 0.0};
        double[] vector12 = new double[] {0.5, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        double[] vector13 = new double[] {0.5, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0};
        double[] vector14 = new double[] {-0.5, -0.5, 1.0, 0.0, 1.0, 0.0, 0.0};
        double[] vector15 = new double[] {-0.5, -0.5, 1.0, 0.0, 0.0, 1.0, 0.0};
        double[] vector16 = new double[] {0.5, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[] vector17 = new double[] {-0.5, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0};
        double[] vector18 = new double[] {0.5, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0};
        double[] vector19 = new double[] {-0.5, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0};
        double[] vector20 = new double[] {-0.5, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0};

        double[] target1 = new double[]  {82.84249423909412, 0.85100000000000002, 0.25847692307692305, 0.0758238511488512, 0.6656992257742256};
        double[] target2 = new double[]  {86.98669256222122, 0.0030211480362537725, 0.09158754136095522, 0.0933498777154366, 0.8150625809236084};
        double[] target3 = new double[]  {89.95439739413683, 0.0032573289902280093, 1.0, 0.0, 0.0};
        double[] target4 = new double[]  {92.88327526132409, 0.0, 1.0, 0.0, 3.868372908101587E-19};
        double[] target5 = new double[]  {95.37192982456146, 0.0, 0.0, 0.0, 1.0};
        double[] target6 = new double[]  {93.82456140350878, 0.0, -0.0, 0.7719298245614035, 0.22807017543859648};
        double[] target7 = new double[]  {95.29032258064517, 0.0, 0.0, 1.0, 0.0};
        double[] target8 = new double[]  {97.78947368421052, 0.0, 1.0, 0.0, 0.0};
        double[] target9 = new double[]  {85.24245149911802, 0.06666666666666668, 0.7175855897914722, 0.19188375350140066, 0.09053065670712733};
        double[] target10 = new double[] {77.59177556293842, 0.0697674418604651, 0.05546142488002956, 0.2988593576965672, 0.6456792174234033};
        double[] target11 = new double[] {81.21156211562116, 0.06642066420664207, 0.050799507995079964, 0.8123616236162365, 0.136838868388684};
        double[] target12 = new double[] {96.3738872403561, 0.013353115727002972, 1.0, -0.0, -0.0};
        double[] target13 = new double[] {92.8780487804878, 0.024390243902439025, -0.0, 1.0, 6.634259537394228E-17};
        double[] target14 = new double[] {85.30857142857144, 0.0, 0.13666666666666666, 0.395, 0.4683333333333333};
        double[] target15 = new double[] {87.10256410256407, 0.019230769230769225, 0.9038461538461539, 0.009615384615384612, 0.08653846153846154};
        double[] target16 = new double[] {92.54545454545453, 0.022727272727272728, -0.0, -0.0, 1.0};
        double[] target17 = new double[] {91.53333333333333, 0.0, -0.0, 1.0, -0.0};
        double[] target18 = new double[] {93.42857142857143, 0.0, -0.0, 0.42857142857142866, 0.5714285714285714};
        double[] target19 = new double[] {90.23684210526316, 0.0, 0.0, 0.0, 1.0};
        double[] target20 = new double[] {90.0,              0.0, -0.0, 1.0, -0.0};


        double[][] input = new double[20][];
        // double[][] target = new double[4][];

        input[0] = vector1;
        input[1] = vector2;
        input[2] = vector3;
        input[3] = vector4;
        input[4] = vector5;
        input[5] = vector6;
        input[6] = vector7;
        input[7] = vector8;
        input[8] = vector9;
        input[9] = vector10;
        input[10] = vector11;
        input[11] = vector12;
        input[12] = vector13;
        input[13] = vector14;
        input[14] = vector15;
        input[15] = vector16;
        input[16] = vector17;
        input[17] = vector18;
        input[18] = vector19;
        input[19] = vector20;

        double[][] target = new double[20][];

        target[0] = target1;
        target[1] = target2;
        target[2] = target3;
        target[3] = target4;
        target[4] = target5;
        target[5] = target6;
        target[6] = target7;
        target[7] = target8;
        target[8] = target9;
        target[9] = target10;
        target[10] = target11;
        target[11] = target12;
        target[12] = target13;
        target[13] = target14;
        target[14] = target15;
        target[15] = target16;
        target[16] = target17;
        target[17] = target18;
        target[18] = target19;
        target[19] = target20;

        SplittableRandom random = new SplittableRandom(0);

        try(TFModel model = new TFModel(
            7,
            5,
            1,
            1,
            TestingTF2.class.getClassLoader().getResourceAsStream("tfModel/graph_BENCHMARK_03.pb").readAllBytes(),
            random)
        )
        {
            for (int i = 0; i < 10000; i++) {
                for (int j = 0; j < 5; j++) {
                    System.out.println("----------------------------------------------------------------" + (j == 2 ? i : ""));
                }

                for (int j = 0; j < input.length; j++) {
                    System.out.println(Arrays.toString(model.predict(input[j])));
                }
                model.fit(input, target);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
