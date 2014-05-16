package org.asoem.greyfish.utils.evolution;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NPointRandomTest {

    @Test
    public void testCreateMutationMap() throws Exception {
        // given
        final int n = 2;
        final Mutations.NPointRandom nPointRandom = new Mutations.NPointRandom(RandomGenerators.rng(), n);

        // when
        final ImmutableMap<Integer, Boolean> mutationMap = nPointRandom.createMutationMap(10);

        // then
        assertThat(mutationMap.size(), is(2));
    }

    @Test
    public void testMutate() throws Exception {
        // given
        final BitString input = BitString.parse("1000");
        final ImmutableMap<Integer, Boolean> mutationMap = ImmutableMap.of(0, true, 3, false);

        // when
        final BitString mutation = Mutations.NPointRandom.mutate(input, mutationMap);

        // then
        assertThat(mutation, is(equalTo(BitString.parse("0001"))));
    }
}