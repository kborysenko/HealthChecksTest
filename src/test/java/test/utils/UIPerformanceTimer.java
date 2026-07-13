package test.utils;

public class UIPerformanceTimer {

    private final long startTime;

    private UIPerformanceTimer() {
        this.startTime = System.currentTimeMillis();
    }

    public static UIPerformanceTimer start() {
        return new UIPerformanceTimer();
    }

    public long elapsedMillis() {
        return System.currentTimeMillis() - startTime;
    }

    public void shouldFinishWithin(long timeoutMillis, String action) {
        long elapsed = elapsedMillis();

        if (elapsed > timeoutMillis) {
            throw new AssertionError(
                    String.format(
                            "%s took %d ms, expected less than %d ms",
                            action,
                            elapsed,
                            timeoutMillis
                    )
            );
        }
    }

}
