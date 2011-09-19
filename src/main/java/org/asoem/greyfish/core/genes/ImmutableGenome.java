package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableComponentList;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.RandomUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImmutableGenome extends ImmutableComponentList<Gene<?>> implements Genome {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableGenome.class);

    private ImmutableGenome(Builder builder) {
        super(builder.genes, builder.agent);
    }

    public ImmutableGenome recombined(final Genome genome) {
        Preconditions.checkArgument(isCompatibleGenome(genome));
        Builder builder = new Builder(null);

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

    protected static class Builder implements BuilderInterface<ImmutableGenome> {
        private final List<Gene<?>> genes = Lists.newArrayList();
        private final Agent agent;

        public Builder(Agent agent) {
            this.agent = checkNotNull(agent);
        }

        public <T extends Gene<?>> Builder add(T gene) { this.genes.add(gene); return this; }
        public <T extends Gene<?>> Builder add(T ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public <T extends Gene<?>> Builder addAll(Iterable<T> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableGenome build() {
            return new ImmutableGenome(this);
        }
    }

    public ImmutableGenome mutated() {
        return new Builder(null).addAll(Iterables.transform(this, new Function<Gene<?>, Gene<?>>() {
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
