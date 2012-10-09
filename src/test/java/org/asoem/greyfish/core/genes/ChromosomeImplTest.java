package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableList;
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
        ChromosomeImpl a = new ChromosomeImpl(ChromosomalHistories.uniparentalHistory(11), ImmutableList.of(foo));
        final Gene<String> bar = new Gene<String>("bar", 0.0);
        ChromosomeImpl b = new ChromosomeImpl(ChromosomalHistories.uniparentalHistory(12), ImmutableList.of(bar));

        // when
        final Chromosome recombined = a.recombined(b);

        // then
        assertThat(recombined.getGenes()).containsOnly(bar);
        assertThat(recombined.getHistory().getParents()).containsOnly(11, 12);
    }
}
