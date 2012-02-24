package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an implementation of {@link ComponentList} which behaves like an {@link com.google.common.collect.ImmutableList}.
 * Therefore it is guaranteed to be immutable, cannot be subclassed and does not permit null elements.
 *
 * <p><b>Note:</b> This implementation delegates to an {@link com.google.common.collect.ImmutableMap} internally to speed up name based retrieval.
 */
public class ImmutableComponentList<E extends AgentComponent> extends ForwardingList<E> implements ComponentList<E> {

    private static final ImmutableComponentList<AgentComponent> EMPTY_COMPONENT_LIST = new ImmutableComponentList<AgentComponent>(ImmutableList.<AgentComponent>of());

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final List<E> listDelegate;

    @SimpleXMLConstructor
    private ImmutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) List<E> components) {
        listDelegate = ImmutableList.copyOf(components);
    }

    @SuppressWarnings("unchecked")
    private ImmutableComponentList(ImmutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(this);

        listDelegate = ImmutableList.copyOf(list.listDelegate);
    }

    @Override
    protected List<E> delegate() {
        return listDelegate;
    }

    public static <E extends AgentComponent> ImmutableComponentList<E> copyOf(Iterable<E> components) {
        checkNotNull(components);
        if (Iterables.size(components) == 0)
            return of();
        else
            return new ImmutableComponentList<E>(ImmutableList.copyOf(components));
    }

    @Nullable
    @Override
    public <T extends E> T find(final String name, final Class<T> clazz) {
        return clazz.cast(Iterables.find(listDelegate, new Predicate<E>() {
            @Override
            public boolean apply(@Nullable E e) {
                assert e != null;
                return e.hasName(name) && clazz.isInstance(e);
            }
        }, null));
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
