package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.DeepCloneable;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 01.02.11
 * Time: 17:03
 * To change this template use File | Settings | File Templates.
 */
public interface GenomeInterface extends DeepCloneable, Iterable<Gene<?>> {
    boolean add(Gene<?> e);

    boolean addAll(Collection<? extends Gene<?>> c);

    Collection<Gene<?>> getGenes();

    void mutate();

    Genome recombine(Genome genome);

    int size();

    void initialize();

    void initGenome(Genome genome);
}
