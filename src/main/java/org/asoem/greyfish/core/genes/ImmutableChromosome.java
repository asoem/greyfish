package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.ImmutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an immutable implementation of a Chromosome in the sense,
 * that it stores its Genes in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored genes.
 */
public class ImmutableChromosome<E extends Gene<?>> extends AbstractChromosome<E> {

    private static final Chromosome<Gene<?>> EMPTY_CHROMOSOME = new ImmutableChromosome<Gene<?>>(ImmutableChromosome.<Gene<?>>of());

    @Element(name = "genes")
    private final ComponentList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableChromosome(@Element(name = "genes") ComponentList<E> genes) {
        delegate = genes;
    }
    
    private ImmutableChromosome(Builder<E> builder) {
        delegate = ImmutableComponentList.copyOf(builder.genes);
    }

    private ImmutableChromosome(ImmutableChromosome<E> immutableGenome, final DeepCloner cloner) {
        cloner.addClone(this);

        delegate = ImmutableComponentList.copyOf(Iterables.transform(immutableGenome, new Function<E, E>() {
            @SuppressWarnings("unchecked") // save downcast
            @Override
            public E apply(@Nullable E gene) {
                assert gene != null;
                return cloner.cloneField(gene, (Class<E>) gene.getClass());
            }
        }));
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(',').join(delegate()) + "]";
    }


    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableChromosome<E>(this, cloner);
    }

    @Override
    protected ComponentList<E> delegate() {
        return delegate;
    }

    public static <E extends Gene<?>> ImmutableChromosome<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build();
    }

    /**
     * Creates a new {@code ImmutableChromosome} which contains {@code ImmutableGene} copies of the geiven {@code genes}
     * @param genes the genes which will used to create new mutated copies from
     * @return a new {@code ImmutableChromosome} with muted copies of the given {@code genes}
     */
    public static ImmutableChromosome<Gene<?>> mutatedCopyOf(Iterable<? extends Gene<?>> genes) {
        return new Builder<Gene<?>>().addAll(Iterables.transform(genes, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> o) {
                return ImmutableGene.newMutatedCopy(o);
            }
        })).build();
    }

    /**
     * Creates a new {@code ImmutableChromosome} out of {@code chromosome1} and {@code chromosome2}.
     * @param chromosome1 The first chromosome
     * @param chromosome2 The second chromosome
     * @return a new {@code ImmutableChromosome}
     */
    public static ImmutableChromosome<? extends Gene<?>> recombined(Chromosome<? extends Gene<?>> chromosome1, Chromosome<? extends Gene<?>> chromosome2) {
        checkNotNull(chromosome1);
        checkNotNull(chromosome2);
        if (chromosome1.size() != chromosome2.size())
            throw new IllegalArgumentException("Genomes differ in size");

        final Builder<Gene<?>> builder = new Builder<Gene<?>>();

        Iterator<? extends Gene<?>> focalIterator = chromosome1.iterator();
        Iterator<? extends Gene<?>> nonFocalIterator = chromosome2.iterator();

        while (focalIterator.hasNext() && nonFocalIterator.hasNext()) {
            Gene<?> next1 =  focalIterator.next();
            Gene<?> next2 =  focalIterator.next();

            final double recombinationProbability = next1.getRecombinationProbability();
            if (recombinationProbability < 0 || recombinationProbability > 1)
                throw new AssertionError("Recombination probability has an invalid value: " + recombinationProbability);

            final boolean recombine = RandomUtils.trueWithProbability(recombinationProbability);
            
            builder.add(recombine ? next1 : next2);
            
            if (recombine) {
                Iterator<? extends Gene<?>> newFocalIterator = nonFocalIterator;
                nonFocalIterator = focalIterator;
                focalIterator = newFocalIterator;
            }
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public static <E extends Gene<?>> Chromosome<E> of() {
        return (Chromosome<E>) EMPTY_CHROMOSOME;
    }

    protected static class Builder<E extends Gene<?>> implements org.asoem.greyfish.utils.base.Builder<ImmutableChromosome<E>> {
        private final List<E> genes = Lists.newArrayList();

        public Builder<E> add(E gene) { this.genes.add(gene); return this; }
        public Builder<E> add(E ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public Builder<E> addAll(Iterable<? extends E> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableChromosome<E> build() {
            return new ImmutableChromosome<E>(this);
        }
    }
}
