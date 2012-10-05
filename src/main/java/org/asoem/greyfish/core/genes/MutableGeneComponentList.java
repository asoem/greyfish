package org.asoem.greyfish.core.genes;

import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.collect.SearchableLists;
import org.simpleframework.xml.Element;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 15:21
 */
public class MutableGeneComponentList<E extends AgentTrait<?>> extends AbstractGeneComponentList<E> {

    @Element(name = "traits")
    private final SearchableList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableGeneComponentList(@Element(name = "traits") SearchableList<E> genes) {
        delegate = genes;
    }

    public MutableGeneComponentList(Iterable<? extends E> genome) {
        delegate = SearchableLists.extend(Lists.newArrayList(genome));
    }

    protected MutableGeneComponentList(MutableGeneComponentList<E> parent, final DeepCloner cloner) {
        cloner.addClone(parent, this);
        delegate = SearchableLists.extend(Lists.newArrayList(Lists.transform(parent.delegate, cloner.<E>cloneFunction())));
    }

    @Override
    protected SearchableList<E> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGeneComponentList<E>(this, cloner);
    }
}
