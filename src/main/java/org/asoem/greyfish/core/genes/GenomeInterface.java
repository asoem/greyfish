package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.DeepCloneable;

import java.util.Collection;

public interface GenomeInterface extends DeepCloneable, Iterable<Gene<?>> {
    boolean add(Gene<?> e);
    boolean addAll(Collection<? extends Gene<?>> c);
    int size();
}
