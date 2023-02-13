package mpern.sap.commerce.build.util;

public class Stopwatch {

    long start;

    long duration = -1;

    public Stopwatch() {
        start = System.currentTimeMillis();
    }

    public long stop() {
        duration = System.currentTimeMillis() - start;
        return duration;
    }

    public long duration() {
        return duration;
    }
}
