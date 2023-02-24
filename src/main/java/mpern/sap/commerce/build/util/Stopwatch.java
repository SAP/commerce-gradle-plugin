package mpern.sap.commerce.build.util;

/**
 * Utility stopwatch.
 */
public class Stopwatch {

    private static final long NANOS_IN_MILLISECOND = 1000000L;

    private final long start;

    private long duration = -1;

    /**
     * Construct and start the watch.
     */
    public Stopwatch() {
        start = System.nanoTime();
    }

    /**
     * Stop the watch and return the duration in milliseconds.
     *
     * @return duration in milliseconds
     */
    public long stop() {
        duration = System.nanoTime() - start;
        return duration / NANOS_IN_MILLISECOND;
    }

    /**
     * Gets the duration in milliseconds.
     *
     * @return duration in ms, -1 if the watch was not stopped
     */
    public long getDurationInMs() {
        return duration / NANOS_IN_MILLISECOND;
    }

    /**
     * Gets the duration in nanoseconds.
     *
     * @return duration in nanos, -1 if the watch was not stopped
     */
    public long getDuration() {
        return duration;
    }
}
