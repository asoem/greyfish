package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This is an immutable implementation of a Genome in the sense,
 * that it stores its Genes in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored genes.
 */
public class ImmutableGenome<E extends Gene<?>> extends ForwardingList<E> implements Genome<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImmutableGenome.class);

    private final ComponentList<E> delegate;

    private ImmutableGenome(Builder<E> builder) {
        delegate = ImmutableComponentList.copyOf(builder.genes);
    }

    @SuppressWarnings("unchecked")
    private ImmutableGenome(ImmutableGenome immutableGenome, final DeepCloner cloner) {
        cloner.setAsCloned(immutableGenome, this);

        delegate = ImmutableComponentList.copyOf(Iterables.transform(immutableGenome, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> gene) {
                return cloner.cloneField(gene, Gene.class);
            }
        }));
    }

    public boolean isCompatibleGenome(Genome<? extends E> genome) {
        if (genome == null || this.size() != genome.size())
            return false;

        final Iterator<? extends E> other_genome_iter = genome.iterator();
        return(Iterables.all(this, new Predicate<Gene<?>>() {
            @Override
            public boolean apply(Gene<?> gene) {
                return other_genome_iter.hasNext() && other_genome_iter.next().isMutatedCopy(gene);
            }
        }));
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(',').join(delegate()) + "]";
    }


    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGenome(this, cloner);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    public static <E extends Gene<?>> ImmutableGenome<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build();
    }

    @Override
    public <T extends E> T get(String name, Class<T> clazz) {
        return delegate.get(name, clazz);
    }

    protected static class Builder<E extends Gene<?>> implements BuilderInterface<ImmutableGenome<E>> {
        private final List<E> genes = Lists.newArrayList();

        public Builder<E> add(E gene) { this.genes.add(gene); return this; }
        public Builder<E> add(E ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public Builder<E> addAll(Iterable<? extends E> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableGenome<E> build() {
            return new ImmutableGenome<E>(this);
        }
    }

    @Override
    public double distance(Genome<? extends E> that) {
        return Genes.normalizedDistance(this, that);
    }

    public Iterable<E> findCopiesFor(final Iterable<? extends E> thisGenes) {

        return Iterables.transform(thisGenes, new Function<E, E>() {
            @Override
            public E apply(final E indexedGene) {
                try {
                    return Iterables.find(delegate(), new Predicate<Gene<?>>() {
                        @Override
                        public boolean apply(Gene<?> gene) {
                            return gene.isMutatedCopy(indexedGene);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ImmutableGenome that = (ImmutableGenome) o;

        if (!delegate.equals(that.delegate)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
