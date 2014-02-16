package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Sampling {

    private Sampling() {}

    /**
     * Randomly sample one element out of {@code e1} and {@code e2}.
     *
     * @param rng the generator to use for sampling
     * @param e1  the first element to sample from
     * @param e2  the second element to sample from
     * @param <S> the type of the elements to sample
     * @return {@code e1} or {@code e2}
     */
    public static <S> S sample(final RandomGenerator rng, final S e1, final S e2) {
        return rng.nextBoolean() ? e1 : e2;
    }

    /**
     * Randomly sample one element from given {@code elements} using {@code rng}.
     *
     * @param elements the elements to sample
     * @param rng      the random number generator to use
     * @return the sampled element
     */
    public static <T> T sample(final Collection<? extends T> elements, final RandomGenerator rng) {
        checkNotNull(rng);
        checkNotNull(elements);
        switch (elements.size()) {
            case 0:
                throw new IllegalArgumentException("Cannot sample element from empty collection");
            case 1:
                return Iterables.getOnlyElement(elements);
            default:
                return Iterables.get(elements, new UniformIntegerDistribution(rng, 0, elements.size() - 1).sample());
        }
    }

    /**
     * <p>Randomly sample {@code n} elements from given {@code collection} without replacement.
     *
     * @param collection the collection to sample from
     * @param n          the number of elements to sample
     * @param rng        the random number generator to use
     * @return an unmodifiable iterable containing randomly but not repeatedly selected elements of the collection
     */
    public static <T> Iterable<T> sampleUnique(final Collection<? extends T> collection, final int n,
                                               final RandomGenerator rng) {
        checkNotNull(rng);
        checkNotNull(collection);
        checkArgument(!collection.isEmpty(),
                "Cannot sample element from empty collection");
        checkArgument(n <= collection.size(),
                "Cannot sample {} unique elements from collection of size {}", n, collection.size());

        if (n == 0) {
            return ImmutableList.of();
        } else if (n / (double) collection.size() < 0.1) {
            return sampleUniqueUsingIndexSet(collection, n, rng);
        } else {
            return sampleUniqueUsingShuffle(collection, n, rng);
        }
    }

    private static <T> Iterable<T> sampleUniqueUsingIndexSet(final Collection<? extends T> collection,
                                                             final int n, final RandomGenerator rng) {
        final UniformIntegerDistribution distribution = new UniformIntegerDistribution(rng, 0, collection.size() - 1);
        final LinkedHashSet<Integer> indexSet = Sets.newLinkedHashSet();
        while (indexSet.size() != n) {
            indexSet.add(distribution.sample());
        }

        return Iterables.transform(indexSet, new Function<Integer, T>() {
            @Nullable
            @Override
            public T apply(final Integer input) {
                return Iterables.get(collection, input);
            }
        });
    }

    private static <T> Iterable<T> sampleUniqueUsingShuffle(final Iterable<? extends T> elements, final int n,
                                                            final RandomGenerator rng) {
        final List<T> in = Lists.newArrayList(elements);
        Collections.shuffle(in, new RandomAdaptor(rng));
        return in.subList(0, n);
    }

    /**
     * Randomly sample {@code n} elements from given {@code elements} using {@code rng}.
     *
     * @param elements the elements to sample
     * @param n        the number of elements to sample
     * @param rng      the random number generator to use
     * @return the collection of sampled elements
     */
    public static <T> Collection<T> sample(final Collection<? extends T> elements, final int n,
                                           final RandomGenerator rng) {
        checkNotNull(rng);
        checkNotNull(elements);

        if (n == 0) {
            return ImmutableList.of();
        }

        switch (elements.size()) {
            case 0:
                throw new IllegalArgumentException("Cannot sample element from empty list");
            case 1:
                final T onlyElement = Iterables.getOnlyElement(elements);
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                for (int i = 0; i < n; i++) {
                    builder.add(onlyElement);
                }
                return builder.build();
            default:
                final UniformIntegerDistribution distribution =
                        new UniformIntegerDistribution(rng, 0, elements.size() - 1);
                final int[] randomIndexes = distribution.sample(n);
                assert randomIndexes.length == n;
                final List<T> list = new ArrayList<>(n);
                for (int randomIndex : randomIndexes) {
                    list.add(Iterables.get(elements, randomIndex));
                }
                return ImmutableList.copyOf(list);
        }
    }
}
