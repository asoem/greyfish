package org.asoem.greyfish.utils.math;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 02.05.12
 * Time: 13:22
 */
public class RandomUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testSampleEmptyCollection() throws Exception {
        // given
        Collection<String> strings = Collections.emptyList();

        // when
        RandomUtils.sample(strings);
    }

    @Test
    public void testSample() throws Exception {
        // given
        Collection<String> strings = Arrays.asList("A", "B", "C");

        // when
        final String sample = RandomUtils.sample(strings);

        // then
        assertThat(sample).isNotNull().isIn(strings);
    }
}
