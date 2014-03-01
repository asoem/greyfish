package org.asoem.greyfish.utils.math.statistics;

import com.google.common.base.Function;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.FunctionalList;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Nullable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class RouletteWheelSelectionTest {
    @Test
    public void testSample() throws Exception {
        // given
        final Sampling<Integer> sampling = new Samplings.RouletteWheelSelection<>(new Function<Integer, Double>() {
            @Nullable
            @Override
            public Double apply(final Integer input) {
                return input == 4 ? 1.0 : 0.0;
            }
        }, mock(RandomGenerator.class));
        final FunctionalList<Integer> integers = ImmutableFunctionalList.of(1, 4, 5, 0, 3);

        // when
        final Iterable<Integer> sample = sampling.sample(integers, 3);

        // then
        assertThat(sample, Matchers.containsInAnyOrder(4, 4, 4));
    }
}
