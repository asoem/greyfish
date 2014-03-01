package org.asoem.greyfish.utils.math.statistics;

import com.google.common.collect.Ordering;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class ElitistSelectionTest {
    @Test
    public void testSample() throws Exception {
        // given
        final Sampling<Comparable<?>> sampling = new Samplings.ElitistSelection<>(Ordering.natural());
        final FunctionalList<Integer> integers = ImmutableFunctionalList.of(1, 4, 5, 0, 3);

        // when
        final Iterable<Integer> sample = sampling.sample(integers, 3);

        // then
        assertThat(sample, Matchers.containsInAnyOrder(5, 4, 3));
    }
}
