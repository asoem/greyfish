package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * User: christoph
 * Date: 31.01.13
 * Time: 11:52
 */
public class LinearSequencesTest {

    @Test
    public void testCrossoverWithFunction() throws Exception {
        // given
        List<String> l1 = ImmutableList.of("x", "x", "y", "y");
        List<String> l2 = ImmutableList.of("y", "y", "x", "x");

        // when
        final Product2<List<String>, List<String>> crossover = LinearSequences.crossover(l1, l2, new Function<Integer, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(Integer input) {
                return input == 2;
            }
        });

        // then
        assertThat(crossover._1(), contains("x", "x", "x", "x"));
        assertThat(crossover._2(), contains("y", "y", "y", "y"));
    }

    @Test
    public void testCrossoverWithProbabilityZero() throws Exception {
        // given
        List<String> l1 = ImmutableList.of("x", "x", "y", "y");
        List<String> l2 = ImmutableList.of("y", "y", "x", "x");

        // when
        final Product2<List<String>, List<String>> crossover = LinearSequences.crossover(l1, l2, 0.0);

        // then
        assertThat(crossover._1(), is(equalTo(l1)));
        assertThat(crossover._2(), is(equalTo(l2)));
    }

    @Test
    public void testCrossoverWithProbabilityOne() throws Exception {
        // given
        List<String> l1 = ImmutableList.of("x", "x", "y", "y");
        List<String> l2 = ImmutableList.of("y", "y", "x", "x");

        // when
        final Product2<List<String>, List<String>> crossover = LinearSequences.crossover(l1, l2, 1.0);

        // then
        assertThat(crossover._1(), contains("y", "x", "x", "y"));
        assertThat(crossover._2(), contains("x", "y", "y", "x"));
    }

    @Test
    public void testHammingDistance() throws Exception {
        // given
        List<String> l1 = ImmutableList.of("x", "x", "x", "y");
        List<String> l2 = ImmutableList.of("x", "y", "x", "x");

        // when
        final int difference = LinearSequences.hammingDistance(l1, l2);

        // then
        assertThat(difference, is(2));
    }
}
