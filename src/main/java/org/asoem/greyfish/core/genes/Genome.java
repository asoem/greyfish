package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;

public class Genome implements GenomeInterface {

    private final List<Gene<?>> genes;

    private Genome(Builder builder) {
        this.genes = ImmutableList.copyOf(builder.genes);
    }

    public List<Gene<?>> getGenes() {
        return genes;
    }

    public Genome recombined(final GenomeInterface genome) {
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

    public ListIterator<Gene<?>> listIterator() {
        return genes.listIterator();
    }

    @Override
    public int size() {
        return genes.size();
    }

    public boolean isCompatibleGenome(GenomeInterface genome) {
        if (genome == null || this.size() != genome.size())
            return false;

        final Iterator<Gene<?>> other_genome_iter = genome.iterator();
        return(Iterables.all(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(Gene<?> gene) {
                return other_genome_iter.hasNext() && other_genome_iter.next().isMutatedCopyOf(gene);
            }
        }));
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(',').join(genes) + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public int indexOf(Gene<?> gene) {
        return genes.indexOf(gene);
    }

    public static class Builder implements BuilderInterface<Genome> {
        private final List<Gene<?>> genes = Lists.newArrayList();

        public <T extends Gene<?>> Builder add(T gene) { this.genes.add(gene); return this; }
        public <T extends Gene<?>> Builder add(T ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public <T extends Gene<?>> Builder addAll(Iterable<T> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public Genome build() {
            return new Genome(this);
        }
    }

    public Genome mutated() {
        return builder().addAll(Iterables.transform(this, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(Gene<?> gene) {
                return DefaultGene.newMutatedCopy(gene);
            }
        })).build();
    }

    @Override
    public double distance(GenomeInterface that) {
        return Genes.normalizedDistance(this, that);
    }

    public Iterable<Gene<?>> findCopiesFor(final Iterable<Gene<?>> thisGenes) {

        return Iterables.transform(thisGenes, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(final Gene<?> indexedGene) {
                try {
                    return Iterables.find(getGenes(), new Predicate<Gene<?>>() {
                        @Override
                        public boolean apply(Gene<?> gene) {
                            return gene.isMutatedCopyOf(indexedGene);
                        }
                    });
                } catch (Exception e) {
                    final String message = "Could not find a match for all genes defined by " + thisGenes +
                            " in the genome '" + this + "'";
                    CORE_LOGGER.error(message, e);
                    throw new IllegalArgumentException(message, e);
                }
            }
        });
    }
}
