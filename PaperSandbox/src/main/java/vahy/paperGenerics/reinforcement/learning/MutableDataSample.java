package vahy.paperGenerics.reinforcement.learning;

public class MutableDataSample {

    private double[] probabilities;
    private double reward = 0.0;
    private double risk = 0;
    private int counter = 0;

    public MutableDataSample(double[] probabilities, double reward, double risk) {
        this.probabilities = probabilities;
        this.reward = reward;
        this.risk = risk;
        counter = 1;
    }

    public MutableDataSample(int actionCount) {
        this.probabilities = new double[actionCount];
    }


    public double[] getProbabilities() {
        return probabilities;
    }

    public double getReward() {
        return reward;
    }

    public double getRisk() {
        return risk;
    }

    public int getCounter() {
        return counter;
    }

    public void addDataSample(double[] newProbabilities, double newReward, double newRisk) {
        reward = ((reward * counter) + newReward) / (counter + 1);
        risk = ((risk * counter) + newRisk) / (counter + 1);
        for (int i = 0; i < newProbabilities.length; i++) {
            probabilities[i] = ((probabilities[i] * counter)  + newProbabilities[i]) / (counter + 1);
        }
        counter++;
    }

    public void addDataSample(MutableDataSample dataSample) {
        this.addDataSample(dataSample.probabilities, dataSample.reward, dataSample.risk);
    }

    public void addDataSamples(MutableDataSample dataSample) {
        reward = (reward * counter + dataSample.reward * dataSample.counter) / (counter + dataSample.counter);
        risk = (risk * counter + dataSample.risk * dataSample.counter) / (counter + dataSample.counter);
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] = (probabilities[i] * counter + dataSample.probabilities[i] * dataSample.counter) / (counter + dataSample.counter);
        }
        counter += dataSample.counter;
    }
}
