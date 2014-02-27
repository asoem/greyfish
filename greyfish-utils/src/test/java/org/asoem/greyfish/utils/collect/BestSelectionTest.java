package org.asoem.greyfish.utils.collect;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class BestSelectionTest {
    @Test
    public void testSample() throws Exception {
        // given
        final Sampling<Comparable<?>> sampling = Samplings.BestSelection.INSTANCE;
        final FunctionalList<Integer> integers = ImmutableFunctionalList.of(1, 4, 5, 0, 3);

        // when
        final Iterable<Integer> sample = sampling.sample(integers, 3);

        // then
        assertThat(sample, Matchers.containsInAnyOrder(5, 5, 5));
    }
}
