package utils;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.concurrent.locks.LockSupport;

public final class BoundedPoller {

    private BoundedPoller() {
    }

    public static void await(String description, Duration timeout, Duration pollInterval, BooleanSupplier condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            LockSupport.parkNanos(pollInterval.toNanos());
        }
        throw new AssertionError("Timed out waiting for " + description + " after " + timeout);
    }
}
