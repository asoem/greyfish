package org.asoem.greyfish.utils.parallel;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import jsr166y.RecursiveAction;
import org.asoem.greyfish.utils.base.VoidFunction;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 19.10.11
 * Time: 14:24
 */
public class ParallelIterables {

    private static final RecursiveAction NULL_ACTION = new RecursiveAction() {
        @Override
        protected void compute() {
            /* NOP */
        }
    };

    private static final VoidFunction<Runnable> COMMAND_EXECUTOR = new VoidFunction<Runnable>() {
        @Override
        public void apply(@Nullable Runnable command) {
            if (command != null) command.run();
        }
    };

    public static RecursiveAction executeAll(Iterable<? extends Runnable> commands, final int threshold) {
        return apply(commands, COMMAND_EXECUTOR, threshold);
    }

    public static <T> RecursiveAction apply(final Iterable<T> iterable, final VoidFunction<? super T> voidFunction, final int threshold) {
        checkNotNull(iterable);
        checkNotNull(voidFunction);

        if (Iterables.isEmpty(iterable)) {
            return NULL_ACTION;
        }
        else {
            return new RecursiveAction() {
                @Override
                protected void compute() {
                    if (Iterables.size(iterable) < threshold) {
                        for (T element : iterable)
                            voidFunction.apply(element);
                    }
                    else {
                        invokeAll(ImmutableList.copyOf(Iterables.transform(Iterables.partition(iterable, threshold), new Function<List<T>, RecursiveAction>() {
                            @Override
                            public RecursiveAction apply(final List<T> elements) {
                                return new RecursiveAction() {
                                    @Override
                                    protected void compute() {
                                        for (T element : elements)
                                            voidFunction.apply(element);
                                    }
                                };
                            }
                        })));
                    }
                }
            };
        }
    }
}
