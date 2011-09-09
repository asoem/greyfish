package org.asoem.greyfish.core.genes;

public interface Genome extends Iterable<Gene<?>> {
//    boolean sum(Gene<?> e);
//    boolean addAll(Collection<? extends Gene<?>> c);
    int size();
    double distance(Genome genome);
    public Iterable<Gene<?>> findCopiesFor(final Iterable<Gene<?>> thisGenes);
    Genome mutated();
    Genome recombined(Genome rws);
}
