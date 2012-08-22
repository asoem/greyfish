package org.asoem.greyfish.core.genes;

import java.util.List;

/**
 * User: christoph
 * Date: 20.08.12
 * Time: 09:35
 */
public interface Chromosome {
    ChromosomalHistory getHistory();

    List<Gene<?>> getGenes();
}
