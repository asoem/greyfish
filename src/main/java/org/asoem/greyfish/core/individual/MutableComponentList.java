package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.HookedForwardingList;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class MutableComponentList<E extends AgentComponent> extends HookedForwardingList<E> implements ComponentList<E> {

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final List<E> delegate;

    @SimpleXMLConstructor
    public MutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) List<E> components) {
        delegate = Lists.newArrayList(components);
    }

    @SuppressWarnings("unchecked")
    public MutableComponentList(MutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(this);
        delegate = Lists.newArrayList(Iterables.transform(list, new Function<E, E>() {
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.cloneField(e, DeepCloneable.class);
            }
        }));
    }

    public MutableComponentList(E ... elements) {
        delegate = Lists.newArrayList(elements);
    }

    public MutableComponentList(Iterable<? extends E> components) {
        delegate = Lists.newArrayList(components);
    }

    @Override
    protected List<E> delegate() {
        return delegate;
    }

    @Nullable
    @Override
    public <T extends E> T find(final String name, Class<T> clazz) {
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

    @Override
    protected void beforeAddition(@Nullable E element) {
        String name = checkNotNull(element).getName();
        for (E e : delegate) {
            if (e.hasName(name))
                throw new IllegalArgumentException("A ComponentList preserves uniqueness of component names." +
                        "Cannot add a second element with name='" + name + "'");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MutableComponentList that = (MutableComponentList) o;

        return delegate.equals(that.delegate);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + delegate.hashCode();
        return result;
    }
}
