package mpern.sap.commerce.build.util;

public class PercentageProgressWriter {

    private final String prefix;
    private final long oneHundred;
    private int lastPercentage;

    public PercentageProgressWriter(String prefix, long oneHundred) {
        this.oneHundred = oneHundred;
        this.prefix = prefix;
        this.lastPercentage = -1;
    }

    public void logProgress(long current) {
        int percentage = (int) Math.floorDiv(current * 100, oneHundred);
        if (percentage > lastPercentage) {
            System.out.printf("\r%s - %d%%", prefix, percentage);
            System.out.flush();
            lastPercentage = percentage;
        }
    }

    public void start() {
        System.out.printf("%n%s", prefix);
        System.out.flush();
    }

    public void finish() {
        System.out.printf("\r%s - FINISHED%n", prefix);
        System.out.flush();
    }
}
