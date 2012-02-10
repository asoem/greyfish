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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This is an immutable implementation of a Genome in the sense,
 * that it stores its Genes in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored genes.
 */
public class ImmutableGenome<E extends Gene<?>> extends AbstractGenome<E> {

    private final ComponentList<E> delegate;

    private ImmutableGenome(Builder<E> builder) {
        delegate = ImmutableComponentList.copyOf(builder.genes);
    }

    private ImmutableGenome(ImmutableGenome<E> immutableGenome, final DeepCloner cloner) {
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
        return new ImmutableGenome<E>(this, cloner);
    }

    @Override
    protected ComponentList<E> delegate() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ImmutableGenome that = (ImmutableGenome) o;

        return delegate.equals(that.delegate);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }

    public static <E extends Gene<?>> ImmutableGenome<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build();
    }

    /**
     * Creates a new {@code ImmutableGenome} which contains {@code ImmutableGene} copies of the geiven {@code genes}
     * @param genes the genes which will used to create new mutated copies from
     * @return a new {@code ImmutableGenome} with muted copies of the given {@code genes}
     */
    public static ImmutableGenome<Gene<?>> mutatedCopyOf(Iterable<? extends Gene<?>> genes) {
        return new Builder<Gene<?>>().addAll(Iterables.transform(genes, new Function<Gene<?>, Gene<?>>() {
            @Override
            public Gene<?> apply(@Nullable Gene<?> o) {
                return ImmutableGene.newMutatedCopy(o);
            }
        })).build();
    }

    /**
     * Creates a new {@code ImmutableGenome} out of {@code genome1} and {@code genome2}
     * by selecting with equal probability one gene of one genome per position.
     * @param genome1 The first genome
     * @param genome2 The second genome
     * @return a new {@code ImmutableGenome}
     */
    public static ImmutableGenome<? extends Gene<?>> recombined(Iterable<Gene<?>> genome1, Genome genome2) {
        final Builder<Gene<?>> builder = new Builder<Gene<?>>();

        final Iterator<Gene<?>> genome1Iterator = genome1.iterator();
        final Iterator genome2Iterator = genome2.iterator();

        while (genome1Iterator.hasNext() && genome2Iterator.hasNext()) {
            Gene<?> next1 =  genome1Iterator.next();
            Gene<?> next2 =  genome1Iterator.next();
            builder.add(RandomUtils.nextBoolean() ? next1 : next2);
        }

        assert ! (genome1Iterator.hasNext() || genome2Iterator.hasNext());

        return builder.build();
    }

    protected static class Builder<E extends Gene<?>> implements org.asoem.greyfish.utils.base.Builder<ImmutableGenome<E>> {
        private final List<E> genes = Lists.newArrayList();

        public Builder<E> add(E gene) { this.genes.add(gene); return this; }
        public Builder<E> add(E ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public Builder<E> addAll(Iterable<? extends E> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableGenome<E> build() {
            return new ImmutableGenome<E>(this);
        }
    }
}
