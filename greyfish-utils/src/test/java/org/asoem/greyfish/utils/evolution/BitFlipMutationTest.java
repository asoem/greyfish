package org.asoem.greyfish.utils.evolution;

import com.google.common.base.Functions;
import org.asoem.greyfish.utils.collect.BitString;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BitFlipMutationTest {
    @Test
    public void testMutate() throws Exception {
        // given
        final Mutations.BitFlipMutation mutation =
                new Mutations.BitFlipMutation(Functions.constant(BitString.parse("0100")));

        // when
        final BitString mutated = mutation.mutate(BitString.parse("0101"));

        // then
        assertThat(mutated, is(equalTo(BitString.parse("0001"))));
    }
}
