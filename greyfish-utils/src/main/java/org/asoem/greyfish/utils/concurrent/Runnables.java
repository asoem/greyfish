package org.asoem.greyfish.utils.concurrent;

public final class Runnables {

    private Runnables() {
        throw new UnsupportedOperationException();
    }

    public static Runnable emptyRunnable() {
        return EmptyRunnable.INSTANCE;
    }

    private static enum EmptyRunnable implements Runnable {
        INSTANCE;

        @Override
        public void run() {
            // empty
        }
    }
}
