package vahy.timer;

public class SimpleTimer {

    private long startingTimeNano;
    private long totalTimeNano;
    private boolean isRunning;

    public boolean isRunning() {
        return isRunning;
    }

    public void startTimer() {
        startingTimeNano = System.nanoTime();
        isRunning = true;
    }

    private long getCurrentTimeDiff() {
        return System.nanoTime() - startingTimeNano;
    }

    private long getTimeDiff() {
        return isRunning ? getCurrentTimeDiff() : totalTimeNano;
    }

    public void stopTimer() {
        totalTimeNano = getTimeDiff();
        isRunning = false;
    }

    public long getTotalTimeInNanos() {
        return getTimeDiff();
    }

    public double getTotalTimeInSeconds() {
        return getTimeDiff() / 1_000_000_000.0;
    }

    public double getTotalTimeInMillis() {
        return totalTimeNano / 1_000_000.0;
    }

    public double getTotalTimeInMicros() {
        return totalTimeNano / 1_000.0;
    }

    public double samplesPerSec(int samples) {
        return (double) samples / (getTimeDiff() / 1_000_000_000.0);
    }
}
