package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GenomeInterface;

import java.util.Iterator;

public class EvaluatedGenome implements GenomeInterface {

    private final double fitness;
    private final GenomeInterface delegate;

    public EvaluatedGenome(GenomeInterface sperm, double fitness) {
        delegate = sperm;
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public String toString() {
        return super.toString() + " (" + fitness + ")";
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public double distance(GenomeInterface genome) {
        return delegate.distance(genome);
    }

    @Override
    public Iterable<Gene<?>> findCopiesFor(Iterable<ForwardingGene<?>> thisGenes) {
        return delegate.findCopiesFor(thisGenes);
    }

    @Override
    public Iterator<Gene<?>> iterator() {
        return delegate.iterator();
    }
}
