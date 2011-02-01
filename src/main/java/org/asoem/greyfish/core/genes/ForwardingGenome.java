package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class ForwardingGenome implements GenomeInterface {

    private Genome genome;

    private ForwardingGenome(ForwardingGenome forwardingGenome, CloneMap cloneMap) {
        this.genome = cloneMap.clone(genome, Genome.class);
    }

    public ForwardingGenome(Genome genome) {
        this.genome = checkNotNull(genome);
    }

    public void setGenome(Genome genome) {
        this.genome = checkNotNull(genome);
    }

    @Override
    public boolean add(Gene<?> e) {
        return genome.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends Gene<?>> c) {
        return genome.addAll(c);
    }

    @Override
    public Collection<Gene<?>> getGenes() {
        return genome.getGenes();
    }

    @Override
    public void mutate() {
        genome.mutate();
    }

    @Override
    public Genome recombine(Genome genome) {
        return this.genome.recombine(genome);
    }

    @Override
    public Iterator<Gene<?>> iterator() {
        return genome.iterator();
    }

    @Override
    public int size() {
        return genome.size();
    }

    @Override
    public void initialize() {
        genome.initialize();
    }

    @Override
    public void initGenome(Genome genome) {
        this.genome.initGenome(genome);
    }

    @Override
    public String toString() {
        return genome.toString();
    }

    @Override
    public <T extends DeepCloneable> T deepClone(Class<T> clazz) {
        return clazz.cast(new ForwardingGenome(this, CloneMap.newInstance()));
    }

    @Override
    public Genome deepCloneHelper(CloneMap map) {
        return genome.deepCloneHelper(map);
    }

    public static ForwardingGenome newInstance(Genome genome) {
        return new ForwardingGenome(genome);
    }
}
