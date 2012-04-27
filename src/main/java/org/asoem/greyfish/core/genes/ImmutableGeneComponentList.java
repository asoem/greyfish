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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * This is an immutable implementation of a GeneComponentList in the sense,
 * that it stores its GenesComponents in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored genes.
 */
public class ImmutableGeneComponentList<E extends GeneComponent<?>> extends AbstractGeneComponentList<E> {

    private static final GeneComponentList<GeneComponent<?>> EMPTY_GENE_COMPONENT_LIST = new ImmutableGeneComponentList<GeneComponent<?>>(ImmutableGeneComponentList.<GeneComponent<?>>of());

    @Element(name = "genes")
    private final ComponentList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableGeneComponentList(@Element(name = "genes") ComponentList<E> genes) {
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
        return new ImmutableGeneComponentList<E>(this, cloner);
    }

    @Override
    protected ComponentList<E> delegate() {
        return delegate;
    }

    public static <E extends GeneComponent<?>> ImmutableGeneComponentList<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build(); // TODO:
    }

    /**
     * Creates a new {@code ImmutableGeneComponentList} which contains {@code ImmutableGeneComponent} copies of the geiven {@code genes}
     * @param genes the genes which will used to create new mutated copies from
     * @return a new {@code ImmutableGeneComponentList} with muted copies of the given {@code genes}
     */
    public static ImmutableGeneComponentList<GeneComponent<?>> mutatedCopyOf(Iterable<? extends GeneComponent<?>> genes) {
        return new Builder<GeneComponent<?>>().addAll(Iterables.transform(genes, new Function<GeneComponent<?>, GeneComponent<?>>() {
            @Override
            public GeneComponent<?> apply(@Nullable GeneComponent<?> o) {
                return ImmutableGeneComponent.newMutatedCopy(o);
            }
        })).build();
    }

    @SuppressWarnings("unchecked")
    public static <E extends GeneComponent<?>> GeneComponentList<E> of() {
        return (GeneComponentList<E>) EMPTY_GENE_COMPONENT_LIST;
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
