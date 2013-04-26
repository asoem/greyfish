package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 16:04
 */
public class ChromosomeImplTest {
    @Test
    public void testRecombined() throws Exception {
        // given
        final TraitVector<String> foo = TraitVector.create("foo", 1.0, typeToken);
        ChromosomeImpl a = new ChromosomeImpl(ImmutableList.of(foo), Sets.newHashSet(11));
        final TraitVector<String> bar = TraitVector.create("bar", 0.0, typeToken);
        ChromosomeImpl b = new ChromosomeImpl(ImmutableList.of(bar), Sets.newHashSet(12));

        // when
        final Chromosome recombined = a.recombined(b);

        // then
        assertThat(recombined.getTraitVectors(), Matchers.<TraitVector<?>>contains(bar));
        assertThat(recombined.getParents(), contains(11, 12));
    }
}
