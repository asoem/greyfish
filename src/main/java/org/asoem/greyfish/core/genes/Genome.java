package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javolution.util.FastList;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Genome implements GenomeInterface {

    private final List<Gene<?>> genes = FastList.newInstance();

    public Genome() {
    }

    private Genome(Genome genome, CloneMap map) {
        for (Gene gene : genome) {
            genes.add(map.clone(gene, Gene.class));
        }
    }

    public Genome(Genome g) {
        Preconditions.checkNotNull(g);

        for (Gene<?> gene : g.getGenes()) {
            add(gene.deepClone(Gene.class));
        }
    }

    public Genome(Iterable<Gene<?>> genes) {
        Iterables.addAll(this.genes, genes);
    }

    @Override
    public boolean add(Gene<?> e) {
        Preconditions.checkNotNull(e);
        return genes.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends Gene<?>> c) {
        Preconditions.checkNotNull(c);
        return genes.addAll(c);
    }

    @Override
    public Collection<Gene<?>> getGenes() {
        return genes;
    }

    @Override
    public void mutate() {
        for (Gene<?> gene : genes) {
            gene.mutate();
        }
    }

    @Override
    public Genome recombine(Genome genome) {
        Preconditions.checkArgument(isCompatibleGenome(genome));

        Genome ret = new Genome(this);

        Iterator<Gene<?>> other_genome_iter = genome.iterator();
        Iterator<Gene<?>> ret_genome_iter = ret.iterator();

        while (ret_genome_iter.hasNext() && other_genome_iter.hasNext()) {

            Gene<?> retGene = ret_genome_iter.next();
            Gene<?> otherGene = other_genome_iter.next();

            if (RandomUtils.nextBoolean())
                retGene.setRepresentation(otherGene.getRepresentation(), null);
        }
        return ret;
    }

    @Override
    public Iterator<Gene<?>> iterator() {
        return genes.iterator();
    }

    @Override
    public int size() {
        return genes.size();
    }

    @Override
    public void initialize() {
        for (Gene<?> gene : this)
            gene.initialize();
    }

    @Override
    public void initGenome(Genome genome) {
        Preconditions.checkArgument(isCompatibleGenome(genome));

        final Iterator<Gene<?>> other_genome_iter = genome.iterator();
        final Iterator<Gene<?>> this_genome_iter = this.iterator();

        while (this_genome_iter.hasNext()
                && other_genome_iter.hasNext()) {
            final Gene<?> thisGene = this_genome_iter.next();
            final Gene<?> newGene = other_genome_iter.next();
            thisGene.setRepresentation(newGene.getRepresentation(), null);
        }
    }

    private boolean isCompatibleGenome(Genome genome) {
        return genome != null
                && genome.size() == this.size();
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(',').join(genes) + "]";
    }

    public static Genome newInstance() {
        return new Genome();
    }

    @Override
    public <T extends DeepCloneable> T deepClone(Class<T> clazz) {
        return clazz.cast(deepCloneHelper(CloneMap.newInstance()));
    }

    @Override
    public Genome deepCloneHelper(CloneMap map) {
        return new Genome(this, map);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements BuilderInterface<Genome> {
        private List<Gene<?>> genes = Lists.newArrayList();

        public Builder add(Gene<?> ... genes) { this.genes.addAll(ImmutableList.of(genes)); return this; }
        public Builder addAll(Iterable<Gene<?>> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public Genome build() {
            return new Genome(genes);
        }
    }
}
