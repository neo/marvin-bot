package io.pivotal.singapore.marvin.utils;

public abstract class ThrowableCatcher {
    public interface AssertedBehaviorCallable {
        void call() throws Throwable;
    }

    public static Throwable capture(AssertedBehaviorCallable callable) {
        try { callable.call(); }
        catch(Throwable throwable) { return throwable; }
        return null;
    }
}
