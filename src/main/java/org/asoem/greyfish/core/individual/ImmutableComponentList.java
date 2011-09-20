package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is an implementation of {@link ComponentList} which behaves like an {@link com.google.common.collect.ImmutableList}.
 * Therefore it is guaranteed to be immutable, cannot be subclassed and does not permit null elements.
 *
 * <p><b>Note:</b> This implementation delegates to an {@link com.google.common.collect.ImmutableMap} internally to speed up name based retrieval.
 */
public class ImmutableComponentList<E extends GFComponent> extends ForwardingList<E> implements ComponentList<E> {

    private final ImmutableMap<String, E> delegate;

    private ImmutableComponentList(Iterable<E> components) {
        checkNotNull(components);
        delegate = Maps.uniqueIndex(components, new Function<E, String>() {
            @Override
            public String apply(@Nullable E component) {
                return checkNotNull(component).getName();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private ImmutableComponentList(ImmutableComponentList<E> list, final DeepCloner cloner) {
        cloner.setAsCloned(list, this);

        delegate = ImmutableMap.copyOf(Maps.transformValues(list.delegate, new Function<E, E>() {
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.continueWith(e, DeepCloneable.class);
            }
        }));
    }

    @Override
    protected List<E> delegate() {
        return delegate.values().asList();
    }

    public static <E extends GFComponent> ImmutableComponentList<E> copyOf(Iterable<E> components) {
        return new ImmutableComponentList<E>(components);
    }

    @Override
    public <T extends E> T get(final String name, Class<T> clazz) {
        return clazz.cast(delegate.get(name));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ImmutableComponentList<E>(this, cloner);
    }
}
