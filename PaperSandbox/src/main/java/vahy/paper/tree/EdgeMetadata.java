package vahy.paper.tree;

public class EdgeMetadata {

    private int visitCount; // N values
    private double meanActionValue; // Q values
    private double totalActionValue; // W values
    private double meanRiskValue;
    private double totalRiskValue;

    private double priorProbability; // P values

    private boolean isPossibleFlag;

    public EdgeMetadata() {
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public double getMeanActionValue() {
        return meanActionValue;
    }

    public void setMeanActionValue(double meanActionValue) {
        this.meanActionValue = meanActionValue;
    }

    public double getTotalActionValue() {
        return totalActionValue;
    }

    public void setTotalActionValue(double totalActionValue) {
        this.totalActionValue = totalActionValue;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public void setPriorProbability(double priorProbability) {
        this.priorProbability = priorProbability;
    }

    public boolean isPossibleFlag() {
        return isPossibleFlag;
    }

    public void setPossibleFlag(boolean possibleFlag) {
        isPossibleFlag = possibleFlag;
    }

    public double getTotalRiskValue() {
        return totalRiskValue;
    }

    public void setTotalRiskValue(double totalRiskValue) {
        this.totalRiskValue = totalRiskValue;
    }

    public double getMeanRiskValue() {
        return meanRiskValue;
    }

    public void setMeanRiskValue(double meanRiskValue) {
        this.meanRiskValue = meanRiskValue;
    }
}
