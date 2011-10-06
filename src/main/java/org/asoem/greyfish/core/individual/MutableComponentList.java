package org.asoem.greyfish.core.individual;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class MutableComponentList<E extends AgentComponent> extends ForwardingList<E> implements ComponentList<E> {

    private final List<E> delegate = Lists.newArrayList();

    @SuppressWarnings("unchecked")
    public MutableComponentList(MutableComponentList<E> list, DeepCloner cloner) {
        for (E e : list)
            delegate.add((E) cloner.continueWith(e, DeepCloneable.class));
    }

    public MutableComponentList() {
    }

    public MutableComponentList(Iterable<E> elements) {
        Iterables.addAll(delegate, elements);
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

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableComponentList<E>(this, cloner);
    }
}
