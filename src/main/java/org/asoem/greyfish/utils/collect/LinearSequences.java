package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.math.RandomUtils;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 31.01.13
 * Time: 11:37
 */
public class LinearSequences {
    private LinearSequences() {}

    public static <E> Product2<List<E>, List<E>> crossover(List<E> x, List<E> y, final double crossoverProbability) {
        return crossover(x, y, new Function<Integer, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable Integer input) {
                return RandomUtils.nextBoolean(crossoverProbability);
            }
        });
    }

    public static <E> Product2<List<E>, List<E>> crossover(List<E> x, List<E> y, final Function<? super Integer, Boolean> crossoverFunction) {
        checkNotNull(x);
        checkNotNull(y);
        checkArgument(x.size() == y.size());
        checkNotNull(crossoverFunction);
        final Tuple2.Zipped<E, List<E>, E, List<E>> zipped = Tuple2.zipped(x, y);
        final Iterable<Product2<E, E>> transform = ImmutableList.copyOf(Iterables.transform(
                zipped,
                new Function<Product2<E, E>, Product2<E, E>>() {
                    int index = 0;
                    boolean b = true;

                    @Override
                    public Product2<E, E> apply(Product2<E, E> input) {
                        if (checkNotNull(crossoverFunction.apply(index++)))
                            b = !b;
                        return b ? input : Tuple2.swap(input);
                    }
                }));
        final Tuple2<Iterable<E>, Iterable<E>> afterCrossover = Tuple2.unzipped(transform);
        return Tuple2.<List<E>, List<E>>of(
                ImmutableList.copyOf(afterCrossover._1()),
                ImmutableList.copyOf(afterCrossover._2()));
    }

    public static <E> int hammingDistance(List<E> a, List<E> b) {
        final Tuple2.Zipped<E, List<E>, E, List<E>> zipped = Tuple2.zipped(a, b);
        int sum = 0;
        for (Product2<E, E> el : zipped) {
            if (!el._1().equals(el._2()))
                ++sum;
        }
        return sum;
    }
}
