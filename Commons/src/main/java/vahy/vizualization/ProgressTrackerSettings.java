package vahy.vizualization;

public class ProgressTrackerSettings {

    private final boolean printOnNextLog;
    private final boolean drawOnNextLog;
    private final boolean printOnEnd;
    private final boolean drawOnEnd;

    public ProgressTrackerSettings(boolean printOnNextLog, boolean drawOnNextLog, boolean printOnEnd, boolean drawOnEnd) {
        this.printOnNextLog = printOnNextLog;
        this.drawOnNextLog = drawOnNextLog;
        this.printOnEnd = printOnEnd;
        this.drawOnEnd = drawOnEnd;
    }

    public boolean isPrintOnNextLog() {
        return printOnNextLog;
    }

    public boolean isDrawOnNextLog() {
        return drawOnNextLog;
    }

    public boolean isPrintOnEnd() {
        return printOnEnd;
    }

    public boolean isDrawOnEnd() {
        return drawOnEnd;
    }
}
