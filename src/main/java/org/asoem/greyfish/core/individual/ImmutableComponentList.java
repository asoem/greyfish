package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

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

    private final ImmutableList<E> listDelegate;

    private ImmutableComponentList(Iterable<E> components) {
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
        return new ImmutableComponentList<E>(components);
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
}
