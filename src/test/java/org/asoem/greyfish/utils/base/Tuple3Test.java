package org.asoem.greyfish.utils.base;

import org.asoem.greyfish.utils.collect.Product3;
import org.asoem.greyfish.utils.collect.Tuple3;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: christoph
 * Date: 06.06.12
 * Time: 11:54
 */
public class Tuple3Test {
    @Test
    public void testZipped() throws Exception {
        // given
        final Tuple3<Set<String>, Set<Double>, Set<Date>> tuple3
                = Tuple3.of(Collections.singleton("a"), Collections.singleton(1.0), Collections.singleton(new Date()));

        // when
        final Iterable<Product3<String, Double, Date>> zipped = tuple3.zipped();


        // then
        assertThat(zipped, is(IsIterableWithSize.<Product3<String, Double, Date>>iterableWithSize(1)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testZippedIllegal() throws Exception {
        // given
        final Tuple3<String, Double, Date> tuple3
                = Tuple3.of("a", 1.0, new Date());

        // when
        tuple3.zipped();
    }
}
