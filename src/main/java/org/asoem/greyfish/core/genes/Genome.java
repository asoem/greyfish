package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    public boolean add(Gene<?> e) {
        Preconditions.checkNotNull(e);
        return genes.add(e);
    }

    public boolean addAll(Collection<? extends Gene<?>> c) {
        Preconditions.checkNotNull(c);
        return genes.addAll(c);
    }

    public Collection<Gene<?>> getGenes() {
        return genes;
    }

    public Genome recombined(final Genome genome) {
        Preconditions.checkArgument(isCompatibleGenome(genome));
        Builder builder = builder();

        Iterator<Gene<?>> other_genome_iter = genome.iterator();
        Iterator<Gene<?>> this_genome_iter = this.iterator();

        while (this_genome_iter.hasNext() && other_genome_iter.hasNext()) {

            Gene<?> retGene = this_genome_iter.next();
            Gene<?> otherGene = other_genome_iter.next();
            builder.add( RandomUtils.nextBoolean() ? retGene : otherGene);
        }

        return builder.build();
    }

    @Override
    public Iterator<Gene<?>> iterator() {
        return genes.iterator();
    }

    public int size() {
        return genes.size();
    }

    private boolean isCompatibleGenome(Genome genome) {
        return genome != null
                && genome.size() == this.size();
    }

    private void isCompatible(Genome genome) {
        checkNotNull(genome);
        checkArgument(this.size() == genome.size());

        final Iterator<Gene<?>> other_genome_iter = genome.iterator();
        checkArgument(Iterables.all(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(Gene<?> gene) {
                return other_genome_iter.hasNext() && other_genome_iter.next().isMutatedVersionOf(gene);
            }
        }));
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

        public <T extends Gene<?>> Builder add(T ... genes) { this.genes.addAll(ImmutableList.copyOf(genes)); return this; }
        public <T extends Gene<?>> Builder addAll(Iterable<T> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public Genome build() {
            return new Genome(genes);
        }
    }

    public Genome mutated() {
        return builder().addAll(Iterables.transform(this, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(Gene<?> gene) {
                return gene.mutatedCopy();
            }
        })).build();
    }
}
