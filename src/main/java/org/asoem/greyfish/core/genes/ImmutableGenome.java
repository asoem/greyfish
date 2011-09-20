package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.ImmutableComponentList;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.RandomUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ImmutableGenome extends ForwardingList<Gene<?>> implements Genome {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableGenome.class);
    private final ComponentList<Gene<?>> delegate;

    private ImmutableGenome(Builder builder) {
        delegate = ImmutableComponentList.copyOf(builder.genes);
    }

    @SuppressWarnings("unchecked")
    public ImmutableGenome(ImmutableGenome immutableGenome, final DeepCloner cloner) {
        cloner.setAsCloned(immutableGenome, this);

        delegate = ImmutableComponentList.copyOf(Iterables.transform(immutableGenome, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> gene) {
                return cloner.continueWith(gene, Gene.class);
            }
        }));
    }

    public ImmutableGenome recombined(final Genome genome) {
        Preconditions.checkArgument(isCompatibleGenome(genome));
        Builder builder = new Builder();

        Iterator<Gene<?>> other_genome_iter = genome.iterator();
        Iterator<Gene<?>> this_genome_iter = this.iterator();

        while (this_genome_iter.hasNext() && other_genome_iter.hasNext()) {

            Gene<?> retGene = this_genome_iter.next();
            Gene<?> otherGene = other_genome_iter.next();
            builder.add( RandomUtils.nextBoolean() ? retGene : otherGene);
        }

        return builder.build();
    }

    public boolean isCompatibleGenome(Genome genome) {
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
        return "[" + Joiner.on(',').join(delegate()) + "]";
    }

    @Override
    public <T extends Gene<?>> T get(String name, Class<T> clazz) {
        return delegate.get(name, clazz);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGenome(this, cloner);
    }

    @Override
    protected List<Gene<?>> delegate() {
        return delegate;
    }

    public static ImmutableGenome copyOf(Iterable<Gene<?>> genes) {
        return new Builder().addAll(genes).build();
    }

    protected static class Builder implements BuilderInterface<ImmutableGenome> {
        private final List<Gene<?>> genes = Lists.newArrayList();

        public <T extends Gene<?>> Builder add(T gene) { this.genes.add(gene); return this; }
        public <T extends Gene<?>> Builder add(T ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public <T extends Gene<?>> Builder addAll(Iterable<T> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableGenome build() {
            return new ImmutableGenome(this);
        }
    }

    public ImmutableGenome mutated() {
        return new Builder().addAll(Iterables.transform(this, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(Gene<?> gene) {
                return ImmutableGene.newMutatedCopy(gene);
            }
        })).build();
    }

    @Override
    public double distance(Genome that) {
        return Genes.normalizedDistance(this, that);
    }

    public Iterable<Gene<?>> findCopiesFor(final Iterable<Gene<?>> thisGenes) {

        return Iterables.transform(thisGenes, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(final Gene<?> indexedGene) {
                try {
                    return Iterables.find(delegate(), new Predicate<Gene<?>>() {
                        @Override
                        public boolean apply(Gene<?> gene) {
                            return gene.isMutatedCopyOf(indexedGene);
                        }
                    });
                } catch (Exception e) {
                    final String message = "Could not find a match for all genes defined by " + thisGenes +
                            " in the genome '" + this + "'";
                    LOGGER.error(message, e);
                    throw new IllegalArgumentException(message, e);
                }
            }
        });
    }
}
