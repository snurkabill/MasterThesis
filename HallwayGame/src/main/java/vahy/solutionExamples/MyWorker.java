package vahy.solutionExamples;

import java.util.concurrent.TimeUnit;

public class MyWorker implements Runnable {

    private final long msToWait;

    public MyWorker(long msToWait) {
        this.msToWait = msToWait;
    }

    @Override
    public void run() {
        try {
            TimeUnit.MILLISECONDS.sleep(msToWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
