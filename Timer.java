//计时器
public class Timer {
    private long startTime;
    private long endTime;

    public Timer() {
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void end() {
        endTime = System.currentTimeMillis();
    }

    public long getEndTimeMs() {
        return endTime - startTime;
    }

    public double getEndTimeS() {
        return getEndTimeMs() / 1000.0;
    }
}