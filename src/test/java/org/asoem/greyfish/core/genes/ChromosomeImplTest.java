package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 16:04
 */
public class ChromosomeImplTest {
    @Test
    public void testRecombined() throws Exception {
        // given
        final Gene<String> foo = new Gene<String>("foo", 1.0);
        ChromosomeImpl a = new ChromosomeImpl(ImmutableList.of(foo), Sets.newHashSet(11));
        final Gene<String> bar = new Gene<String>("bar", 0.0);
        ChromosomeImpl b = new ChromosomeImpl(ImmutableList.of(bar), Sets.newHashSet(12));

        // when
        final Chromosome recombined = a.recombined(b);

        // then
        assertThat(recombined.getGenes()).containsOnly(bar);
        assertThat(recombined.getParents()).containsOnly(11, 12);
    }
}
