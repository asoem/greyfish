package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 11:18
 */
public class ImmutableComponentList<E extends GFComponent> extends ForwardingList<E> implements ComponentList<E> {

    private final List<E> delegate;

    protected ImmutableComponentList(Iterable<? extends E> components) {
        delegate = ImmutableList.copyOf(components);

    }

    @SuppressWarnings("unchecked")
    protected ImmutableComponentList(ImmutableComponentList list, CloneMap map) {
        map.insert(list, this);
        delegate = ImmutableList.copyOf(map.cloneAll(list.delegate, Object.class));
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    public static <E extends GFComponent> ImmutableComponentList<E> copyOf(Iterable<? extends E> components) {
        return new ImmutableComponentList<E>(components);
    }

    @Override
    public <T extends E> T get(final String name, Class<T> clazz) {
        return clazz.cast(Iterables.find(delegate, new Predicate<E>() {
            @Override
            public boolean apply(@Nullable E e) {
                assert e != null;
                return e.hasName(name);
            }
        }, null));
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new ImmutableComponentList(this, map);
    }
}
