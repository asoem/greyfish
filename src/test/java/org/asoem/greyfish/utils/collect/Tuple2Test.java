package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: christoph
 * Date: 31.01.13
 * Time: 12:12
 */
public class Tuple2Test {
    @Test
    public void testZipped() throws Exception {
        // given
        final ImmutableList<String> l1 = ImmutableList.of("a", "b");
        final ImmutableList<String> l2 = ImmutableList.of("a", "b");

        // when
        final Tuple2.Zipped<String, ImmutableList<String>, String, ImmutableList<String>> zipped = Tuple2.zipped(l1, l2);

        // then
        assertThat(zipped, Matchers.<Product2<String, String>>contains(Tuple2.of("a", "a"), Tuple2.of("b", "b")));
    }
}
