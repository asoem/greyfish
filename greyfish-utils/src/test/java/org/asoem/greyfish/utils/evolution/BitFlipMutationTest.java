/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
