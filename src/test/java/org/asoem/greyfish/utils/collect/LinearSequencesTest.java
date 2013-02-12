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

/**
 * User: christoph
 * Date: 31.01.13
 * Time: 11:52
 */
public class LinearSequencesTest {

    @Test
    public void testCrossoverWithIndexSet() throws Exception {
        // given
        final List<String> l1 = ImmutableList.of("x", "x", "y", "y");
        final List<String> l2 = ImmutableList.of("y", "y", "x", "x");

        // when
        final Product2<Iterable<String>, Iterable<String>> crossover =
                LinearSequences.crossover(l1, l2, Sets.newHashSet(2));

        // then
        assertThat(crossover._1(), contains("x", "x", "x", "x"));
        assertThat(crossover._2(), contains("y", "y", "y", "y"));
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
        assertThat(crossover._2(), contains(l2.toArray(new String[l2.size()])));
    }

    @Test
    public void testCrossoverUnequalLength() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("xxxx");
        final ImmutableList<Character> l2 = Lists.charactersOf("yyyyz");

        // when
        final Product2<Iterable<Character>, Iterable<Character>> crossover = LinearSequences.crossover(l1, l2, ImmutableSet.<Integer>of());

        // then
        assertThat(crossover._1(), contains(Lists.charactersOf("xxxx").toArray(new Character[4])));
        assertThat(crossover._2(), contains(Lists.charactersOf("yyyy").toArray(new Character[4])));
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
