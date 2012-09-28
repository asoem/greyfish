package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.collect.TinyLists;
import org.simpleframework.xml.ElementList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an implementation of {@link ComponentList} which behaves like an {@link com.google.common.collect.ImmutableList}.
 * Therefore it is guaranteed to be immutable, cannot be subclassed and does not permit null elements.
 *
 * <p><b>Note:</b> This implementation delegates to an {@link com.google.common.collect.ImmutableMap} internally to stepSize up name based retrieval.
 */
public class ImmutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> {

    private static final ImmutableComponentList<AgentComponent> EMPTY_COMPONENT_LIST =
            new ImmutableComponentList<AgentComponent>(TinyLists.<AgentComponent>of());

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final SearchableList<E> listDelegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ImmutableComponentList(
            @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) SearchableList<E> components) {
        listDelegate = components;
    }

    @SuppressWarnings("unchecked")
    private ImmutableComponentList(ImmutableComponentList<E> list, DeepCloner cloner) {
        cloner.addClone(this);
        listDelegate = TinyLists.transform(list.listDelegate, cloner.<E>cloneFunction());
    }

    @Override
    protected SearchableList<E> delegate() {
        return listDelegate;
    }

    public static <E extends AgentComponent> ImmutableComponentList<E> copyOf(Iterable<E> components) {
        checkNotNull(components);
        if (Iterables.size(components) == 0)
            return of();
        else
            return new ImmutableComponentList<E>(TinyLists.copyOf(components));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableComponentList<E>(this, cloner);
    }

    @SuppressWarnings("unchecked")
    public static <E extends AgentComponent> ImmutableComponentList<E> of() {
        return (ImmutableComponentList<E>) EMPTY_COMPONENT_LIST;
    }

}
