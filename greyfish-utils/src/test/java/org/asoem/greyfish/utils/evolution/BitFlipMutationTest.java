package org.asoem.greyfish.utils.evolution;

import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BitFlipMutationTest {
    @Test
    public void testMutate() throws Exception {
        // given
        final BitString input = BitString.parse("0101");
        final BitString flipTemplate = BitString.parse("0100");

        // when
        final BitString mutated = Mutations.BitFlipMutation.mutate(input, flipTemplate);

        // then
        assertThat(mutated, is(equalTo(BitString.parse("0001"))));
    }

    @Ignore
    @Test
    public void testMutationFlow() throws Exception {
        // given
        final BitString startString = BitString.random(10000, RandomGenerators.rng(), 0.4);
        final Mutations.BitFlipMutation mutation =
                new Mutations.BitFlipMutation(RandomGenerators.rng(), 1.0 / startString.size());
        final File file = new File("mutation_test.csv");
        final CharSink charSink = Files.asCharSink(file, Charset.defaultCharset());

        // when

        try (final Writer writer = charSink.openBufferedStream()) {
            BitString current = startString;
            for (int i = 0; i < 100000; i++) {
                writer.write(String.valueOf(-40.0 + (double) current.cardinality() / 10000 * 80.0));
                writer.write("\n");
                current = mutation.mutate(current);
            }
        }

        // then
    }
}
