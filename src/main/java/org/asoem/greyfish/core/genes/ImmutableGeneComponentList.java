package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.ImmutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.Element;

import java.util.Arrays;
import java.util.List;

/**
 * This is an immutable implementation of a GeneComponentList in the sense,
 * that it stores its GenesComponents in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored genes.
 */
public class ImmutableGeneComponentList<E extends GeneComponent<?>> extends AbstractGeneComponentList<E> {

    private static final ImmutableGeneComponentList<GeneComponent<?>> EMPTY_GENE_COMPONENT_LIST = ImmutableGeneComponentList.of();

    @Element(name = "genes")
    private final ComponentList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableGeneComponentList(@Element(name = "genes") ImmutableComponentList<E> genes) {
        delegate = genes;
    }
    
    private ImmutableGeneComponentList(Builder<E> builder) {
        delegate = ImmutableComponentList.copyOf(builder.genes);
    }

    private ImmutableGeneComponentList(ImmutableGeneComponentList<E> immutableGenome, final DeepCloner cloner) {
        cloner.addClone(this);

        delegate = ImmutableComponentList.copyOf(Iterables.transform(immutableGenome, new Function<E, E>() {
            @SuppressWarnings("unchecked") // save downcast
            @Override
            public E apply(E gene) {
                assert gene != null;
                return cloner.getClone(gene, (Class<E>) gene.getClass());
            }
        }));
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(',').join(delegate()) + "]";
    }


    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableGeneComponentList<E>(this, cloner);
    }

    @Override
    protected ComponentList<E> delegate() {
        return delegate;
    }

    public static <E extends GeneComponent<?>> ImmutableGeneComponentList<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build(); // TODO:
    }

    @SuppressWarnings("unchecked")
    public static <E extends GeneComponent<?>> ImmutableGeneComponentList<E> of() {
        return (ImmutableGeneComponentList<E>) EMPTY_GENE_COMPONENT_LIST;
    }

    protected static class Builder<E extends GeneComponent<?>> implements org.asoem.greyfish.utils.base.Builder<ImmutableGeneComponentList<E>> {
        private final List<E> genes = Lists.newArrayList();

        public Builder<E> add(E gene) { this.genes.add(gene); return this; }
        public Builder<E> add(E ... genes) { this.genes.addAll(Arrays.asList(genes)); return this; }
        public Builder<E> addAll(Iterable<? extends E> genes) { Iterables.addAll(this.genes, genes); return this; }

        @Override
        public ImmutableGeneComponentList<E> build() {
            return new ImmutableGeneComponentList<E>(this);
        }
    }
}
