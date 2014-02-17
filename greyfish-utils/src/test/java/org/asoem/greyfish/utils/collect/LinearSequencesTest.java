package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;


public class LinearSequencesTest {

    @Test
    public void testCrossoverWithIndexSet() throws Exception {
        // given
        final List<String> l1 = ImmutableList.of("x", "x", "x", "x");
        final List<String> l2 = ImmutableList.of("y", "y", "y", "y");

        // when
        final Product2<Iterable<String>, Iterable<String>> crossover =
                LinearSequences.crossover(l1, l2, Sets.newHashSet(1, 3));

        // then
        assertThat(crossover.first(), contains("x", "y", "y", "y"));
        assertThat(crossover.second(), contains("y", "x", "x", "x"));
    }

    @Test
    public void testCrossoverMutability() throws Exception {
        // given
        final Iterable<String> l1 = ImmutableList.of("x", "x", "y", "y");
        final List<String> l2 = Lists.newArrayList("y", "y", "x", "x");
        final Product2<Iterable<String>, Iterable<String>> crossover = LinearSequences.crossover(l1, l2, ImmutableSet.<Integer>of());

        // when
        l2.set(3, "y");

        // then
        assertThat(crossover.second(), contains(l2.toArray(new String[l2.size()])));
    }

    @Test
    public void testCrossoverUnequalLength() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("xxxx");
        final ImmutableList<Character> l2 = Lists.charactersOf("yyyyz");

        // when
        final Product2<Iterable<Character>, Iterable<Character>> crossover = LinearSequences.crossover(l1, l2, ImmutableSet.<Integer>of());

        // then
        assertThat(crossover.first(), contains(Lists.charactersOf("xxxx").toArray(new Character[4])));
        assertThat(crossover.second(), contains(Lists.charactersOf("yyyy").toArray(new Character[4])));
    }

    @Test
    public void testHammingDistance() throws Exception {
        // given
        final List<String> l1 = ImmutableList.of("x", "x", "x", "y");
        final List<String> l2 = ImmutableList.of("x", "y", "x", "x");

        // when
        final int difference = LinearSequences.hammingDistance(l1, l2);

        // then
        assertThat(difference, is(2));
    }
}
