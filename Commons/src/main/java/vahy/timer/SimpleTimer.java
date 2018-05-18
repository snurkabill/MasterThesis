package vahy.timer;

public class SimpleTimer {

    private long startingTime;
    private long totalTime;
    private boolean isRunning;

    public boolean isRunning() {
        return isRunning;
    }

    public void startTimer() {
        startingTime = System.nanoTime();
        isRunning = true;
    }

    public void stopTimer() {
        totalTime = System.nanoTime() - startingTime;
        isRunning = false;
    }

    public long getTotalTimeInMillis() {
        return totalTime;
    }

    public double secondsSpent() {
        return totalTime / 1000.0;
    }

    public double samplesPerSec(int samples) {
        return (double) samples / (getTotalTimeInMillis() / 1000.0);
    }
}
