package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.HookedForwardingList;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public abstract class MutableComponentList<E extends GFComponent> extends HookedForwardingList<E> implements ComponentList<E> {

    private final List<E> delegate = Lists.newArrayList();

    protected abstract Agent getAgent();

    public static <T extends GFComponent> MutableComponentList<T> ownedBy(final Agent agent) {
        return new MutableComponentList<T>() {
            @Override
            protected Agent getAgent() {
                return agent;
            }
        };
    }

    @Override
    protected void beforeAddition(@Nullable E element) {
        checkNotNull(element);
    }

    @Override
    protected void afterAddition(@Nullable E element) {
        assert element != null;
        element.setAgent(getAgent());
    }

    @Override
    protected void beforeRemoval(@Nullable E element) {
        checkNotNull(element);
    }

    @Override
    protected void afterRemoval(@Nullable E element) {
        assert element != null;
        element.setAgent(null);
    }

    @Override
    protected void beforeReplacement(@Nullable E oldElement, @Nullable E newElement) {
        checkNotNull(newElement);
    }

    @Override
    protected void afterReplacement(@Nullable E oldElement, @Nullable E newElement) {
        assert oldElement != null;
        oldElement.setAgent(null);
        assert newElement != null;
        newElement.setAgent(getAgent());
    }

    @Override
    protected List<E> delegate() {
        return delegate;
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
