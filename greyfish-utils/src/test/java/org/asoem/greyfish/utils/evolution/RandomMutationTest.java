package org.asoem.greyfish.utils.evolution;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.collect.BitSets;
import org.asoem.greyfish.utils.collect.BitString;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RandomMutationTest {

    @Test
    public void testMutate() throws Exception {
        // given
        final ArrayList<MutateTestData> mutateTestDataList = Lists.newArrayList(
                new MutateTestData(BitString.parse("0000"), BitString.parse("0000"),
                        BitString.parse("1111"), BitString.parse("0000")),
                new MutateTestData(BitString.parse("0000"), BitString.parse("1111"),
                        BitString.parse("0000"), BitString.parse("0000")),
                new MutateTestData(BitString.parse("1100"), BitString.parse("1001"),
                        BitString.parse("0011"), BitString.parse("0101")),
                new MutateTestData(BitString.zeros(10000), BitString.forIndices(ImmutableSortedSet.of(0), 10000),
                        BitString.forIndices(ImmutableSortedSet.of(0), 10000), BitString.forIndices(ImmutableSortedSet.of(0), 10000)),
                new MutateTestData(BitString.create(128, 0L, ~0L), BitString.forIndices(ImmutableSortedSet.of(0, 127), 128),
                        BitString.create(128, 1L, ~0L), BitString.create(128, 1L, ~0L))
        );

        for (MutateTestData mutateTestData : mutateTestDataList) {
            testMutateHelper(mutateTestData);
        }
    }

    public void testMutateHelper(MutateTestData input) throws Exception {
        // given
        // the input

        // when
        final BitString mutated = Mutations.RandomMutation.mutate(
                input.input, input.positionTemplate, input.mutationTemplate);

        // then
        assertThat(mutated, is(equalTo(input.expected)));
    }

    private static class MutateTestData {
        final BitString input;
        final BitString positionTemplate;
        final BitString mutationTemplate;
        final BitString expected;

        private MutateTestData(final BitString input, final BitString positionTemplate,
                               final BitString mutationTemplate, final BitString expected) {
            this.input = input;
            this.positionTemplate = positionTemplate;
            this.mutationTemplate = mutationTemplate;
            this.expected = expected;
        }
    }

    @Ignore
    @Test
    public void testMutationFlow() throws Exception {
        // given
        final int length = 10000;
        final BitString startString = BitString.random(length, RandomGenerators.rng());
        final Mutations.RandomMutation mutation =
                new Mutations.RandomMutation(RandomGenerators.rng(), 1.0 / startString.size());
        final File file = new File("mutation_test.csv");
        final CharSink charSink = Files.asCharSink(file, Charset.defaultCharset());

        // when

        try (final Writer writer = charSink.openBufferedStream()) {
            BitString current = startString;
            for (int i = 0; i < 100000; i++) {
                final double d = -40.0 + (double) current.cardinality() / length * 80.0;
                writer.write(String.valueOf(d));
                writer.write("\n");
                current = mutation.mutate(current);
            }
        }

        // then
    }

    @Ignore
    @Test
    public void testMutationFlow2() throws Exception {
        // given
        final int length = 10000;
        final BitString startString = BitString.random(length, RandomGenerators.rng(), 0.4);
        final Mutation<BitString> mutation = new Mutation<BitString>() {
            @Override
            public BitString mutate(final BitString input) {
                final RandomGenerator rng = RandomGenerators.rng();
                final int position = RandomGenerators.nextInt(rng, 0, input.size());
                final BitSet bitSet = BitSets.create(input);
                bitSet.set(position, rng.nextBoolean());
                return BitString.forBitSet(bitSet, input.size());
            }
        };
        final File file = new File("mutation_test.csv");
        final CharSink charSink = Files.asCharSink(file, Charset.defaultCharset());

        // when

        try (final Writer writer = charSink.openBufferedStream()) {
            BitString current = startString;
            for (int i = 0; i < 100000; i++) {
                final double d = -40.0 + (double) current.cardinality() / length * 80.0;
                writer.write(String.valueOf(d));
                writer.write("\n");
                current = mutation.mutate(current);
            }
        }

        // then
    }

    @Ignore
    @Test
    public void testMutationFlow3() throws Exception {
        // given
        final int length = 10000;
        final RandomGenerator rng = RandomGenerators.rng();

        final File file = new File("mutation_test.csv");
        final CharSink charSink = Files.asCharSink(file, Charset.defaultCharset());

        // when

        try (final Writer writer = charSink.openBufferedStream()) {
            for (int i = 0; i < 100000; i++) {
                final BitString positionTemplate = BitString.random(length, rng, 1.0 / length);
                final BitString mutationTemplate = BitString.random(length, rng, 0.5);

                final int positions = positionTemplate.cardinality();
                final int ones = positionTemplate.and(mutationTemplate).cardinality();

                writer.write(String.valueOf(positions));
                writer.write(",");
                writer.write(String.valueOf(ones));
                writer.write("\n");
            }
        }

        // then
    }
}