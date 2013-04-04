package org.asoem.greyfish.utils.math;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

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
        List<String> samples = Lists.newArrayList();

        // when
        for (int i = 0; i < 1000; i++) {
            samples.add(RandomUtils.sample(strings));
        }

        // then
        assertThat(samples, everyItem(isIn(strings)));
    }
}
