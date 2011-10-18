package org.asoem.greyfish.core.concurrent;

import com.google.common.base.Supplier;
import jsr166y.ForkJoinPool;
import jsr166y.ForkJoinTask;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.utils.ParallelListTask;

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

    public static <T> T invoke(ForkJoinTask<T> task) {
        return INSTANCE.get().invoke(task);
    }

    public static void execute(ParallelListTask<Agent> task) {
        INSTANCE.get().execute(task);
    }

    /*
    Lazy initialization holder class idiom for static fields
    See Item 71 in EffectiveJava
    */
    private static class PoolHolder {
        static final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }
}
