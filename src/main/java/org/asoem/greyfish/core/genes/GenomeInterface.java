package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

public interface GenomeInterface extends DeepCloneable, Iterable<Gene<?>> {
//    boolean add(Gene<?> e);
//    boolean addAll(Collection<? extends Gene<?>> c);
    int size();
    double distance(GenomeInterface genome);

    public Gene<?> get(int index);

    @Override
    GenomeInterface deepCloneHelper(CloneMap map);
}
