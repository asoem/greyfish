package org.asoem.greyfish.utils.collect;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * User: christoph
 * Date: 10.02.13
 * Time: 14:20
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
}
