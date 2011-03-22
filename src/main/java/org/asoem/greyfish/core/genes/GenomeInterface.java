package org.asoem.greyfish.core.genes;

public interface GenomeInterface extends Iterable<Gene<?>> {
//    boolean sum(Gene<?> e);
//    boolean addAll(Collection<? extends Gene<?>> c);
    int size();
    double distance(GenomeInterface genome);
    public Iterable<Gene<?>> findCopiesFor(final Iterable<Gene<?>> thisGenes);
}
