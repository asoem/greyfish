package org.asoem.greyfish.core.genes;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.agent.ImmutableComponentList;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.TinyList;
import org.asoem.greyfish.utils.collect.TinyLists;
import org.simpleframework.xml.Element;

import java.util.Arrays;
import java.util.List;

/**
 * This is an immutable implementation of a GeneComponentList in the sense,
 * that it stores its GenesComponents in an {@link ImmutableComponentList},
 * but makes no guarantees about the immutability of the stored traits.
 */
public class ImmutableGeneComponentList<E extends AgentTrait<?>> extends AbstractGeneComponentList<E> {

    private static final ImmutableGeneComponentList<AgentTrait<?>> EMPTY_AGENT_TRAIT_LIST = ImmutableGeneComponentList.of();

    @Element(name = "traits")
    private final TinyList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableGeneComponentList(@Element(name = "traits") TinyList<E> genes) {
        delegate = genes;
    }
    
    private ImmutableGeneComponentList(Builder<E> builder) {
        delegate = TinyLists.copyOf(builder.genes);
    }

    private ImmutableGeneComponentList(ImmutableGeneComponentList<E> immutableGenome, final DeepCloner cloner) {
        cloner.addClone(immutableGenome, this);

        delegate = TinyLists.transform(immutableGenome.delegate, cloner.<E>cloneFunction());
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
    protected TinyList<E> delegate() {
        return delegate;
    }

    public static <E extends AgentTrait<?>> ImmutableGeneComponentList<E> copyOf(Iterable<? extends E> genes) {
        return new Builder<E>().addAll(genes).build(); // TODO:
    }

    @SuppressWarnings("unchecked")
    public static <E extends AgentTrait<?>> ImmutableGeneComponentList<E> of() {
        return (ImmutableGeneComponentList<E>) EMPTY_AGENT_TRAIT_LIST;
    }

    protected static class Builder<E extends AgentTrait<?>> implements org.asoem.greyfish.utils.base.Builder<ImmutableGeneComponentList<E>> {
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
