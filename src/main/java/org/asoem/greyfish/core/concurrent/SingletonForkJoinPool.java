package org.asoem.greyfish.core.concurrent;

import com.google.common.base.Supplier;
import jsr166y.ForkJoinPool;
import jsr166y.ForkJoinTask;

/**
 * User: christoph
 * Date: 12.10.11
 * Time: 09:44
 */
public enum SingletonForkJoinPool implements Supplier<ForkJoinPool> {

    INSTANCE;

    @Override
    public ForkJoinPool get() {
        return PoolHolder.pool;
    }

    public static ForkJoinPool forkJoinPool() {
        return INSTANCE.get();
    }

    /**
     * Delegates to {@link ForkJoinPool#invoke(jsr166y.ForkJoinTask)}
     * @param task the task to invoke with the {@link ForkJoinPool} instance managed by this class
     * @param <T> the task to invoke
     * @return the value computed by the {@code task}
     */
    public static <T> T invoke(ForkJoinTask<T> task) {
        return INSTANCE.get().invoke(task);
    }

    public static void execute(ForkJoinTask<?> task) {
        INSTANCE.get().execute(task);
    }

    /*
    Lazy initialization holder class idiom for static fields
    See Item 71 in EffectiveJava
    */
    private static class PoolHolder {
        static final ForkJoinPool pool = new ForkJoinPool();
    }

    private static int parallelism() {
        return forkJoinPool().getParallelism();
    }
}
