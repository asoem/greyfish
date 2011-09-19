package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 11:18
 */
public abstract class ImmutableComponentList<E extends GFComponent> extends ForwardingList<E> implements ComponentList<E> {

    private final List<E> delegate;

    protected abstract Agent getAgent();

    protected ImmutableComponentList(Iterable<? extends E> components) {
        delegate = ImmutableList.copyOf(components);
        for (E component : components)
            component.setAgent(getAgent());
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    public static <E extends GFComponent> ImmutableComponentList<E> copyOf(Iterable<? extends E> components, final Agent owner) {
        return new ImmutableComponentList<E>(components) {

            @Override
            protected Agent getAgent() {
                return owner;
            }
        };
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
}
