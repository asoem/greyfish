package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph Date: 10.02.13 Time: 14:20
 */
public class ProductsTest {
    @Test
    public void testZip() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("ab");
        final ImmutableList<Character> l2 = Lists.charactersOf("ab");

        // when
        final Iterable<Product2<Character, Character>> zipped = Products.zip(l1, l2);

        // then
        assertThat(zipped, Matchers.<Product2<Character, Character>>contains(Tuple2.of('a', 'a'), Tuple2.of('b', 'b')));
    }

    @Test
    public void testZipUnequalLength() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("ab");
        final ImmutableList<Character> l2 = Lists.charactersOf("abc");

        // when
        final Iterable<Product2<Character, Character>> zipped = Products.zip(l1, l2);

        // then
        assertThat(zipped, Matchers.<Product2<Character, Character>>contains(Tuple2.of('a', 'a'), Tuple2.of('b', 'b')));
    }

    @Test
    public void testZipAllFewerInFirst() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("ab");
        final ImmutableList<Character> l2 = Lists.charactersOf("abc");

        // when
        final Iterable<Product2<Character, Character>> zipAll = Products.zipAll(l1, l2, 'c', 'd');

        // then
        assertThat(zipAll, Matchers.<Product2<Character, Character>>contains(Tuple2.of('a', 'a'), Tuple2.of('b', 'b'), Tuple2.of('c', 'c')));
    }

    @Test
    public void testZipAllFewerInSecond() throws Exception {
        // given
        final ImmutableList<Character> l1 = Lists.charactersOf("abc");
        final ImmutableList<Character> l2 = Lists.charactersOf("ab");

        // when
        final Iterable<Product2<Character, Character>> zipAll = Products.zipAll(l1, l2, 'd', 'c');

        // then
        assertThat(zipAll, Matchers.<Product2<Character, Character>>contains(Tuple2.of('a', 'a'), Tuple2.of('b', 'b'), Tuple2.of('c', 'c')));
    }

    @Test
    public void testZipped() throws Exception {
        // given

        // when
        final Iterable<Product3<String, Double, Date>> zipped = Products.zip(Collections.singleton("a"), Collections.singleton(1.0), Collections.singleton(new Date()));

        // then
        Assert.assertThat(zipped, is(IsIterableWithSize.<Product3<String, Double, Date>>iterableWithSize(1)));
    }

    @Test
    public void testZip3() throws Exception {
        // given
        final Tuple3<ImmutableList<String>, ImmutableList<String>, ImmutableList<String>> zipped =
                Tuple3.of(ImmutableList.of("a"), ImmutableList.of("b"), ImmutableList.of("c"));

        // when
        final Iterable<Product3<String, String, String>> zip = Products.zip(zipped);

        // then
        assertThat(zip, contains(new CustomTypeSafeMatcher<Product3<String, String, String>>("") {
            @Override
            protected boolean matchesSafely(final Product3<String, String, String> item) {
                return item.first().equals("a") && item.second().equals("b") && item.third().equals("c");
            }
        }));
    }
}
