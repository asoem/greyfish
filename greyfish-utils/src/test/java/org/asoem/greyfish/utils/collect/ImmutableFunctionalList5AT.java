package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Test;

import javax.annotation.Nullable;

public class ImmutableFunctionalList5AT extends ImmutableFunctionalListAT {
    @Test
    public void testFindFirst() throws Exception {
        final int runs = 1000;
        final ImmutableFunctionalList<String> functionalList = new ImmutableFunctionalList5<>("a", "b", "c", "d", "e");
        final ImmutableList<String> immutableList = ImmutableList.copyOf(functionalList);
        final int predicateCount = 10000;
        final Iterable<Predicate<String>> predicates = Iterables.transform(
                RandomGenerators.sample(RandomGenerators.rng(), immutableList, predicateCount),
                new Function<String, Predicate<String>>() {
                    @Nullable
                    @Override
                    public Predicate<String> apply(@Nullable final String input) {
                        return Predicates.equalTo(input);
                    }
                });

        testFindFirst(runs, immutableList, functionalList, predicates);
    }
}
