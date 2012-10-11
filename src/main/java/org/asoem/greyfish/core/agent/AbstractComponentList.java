package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedList;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * User: christoph
 * Date: 22.09.12
 * Time: 12:59
 */
public abstract class AbstractComponentList<E extends AgentComponent> extends ForwardingList<E> implements ComponentList<E> {

    @Override
    protected abstract AugmentedList<E> delegate();

    @Nullable
    @Override
    public <T extends E> T find(final String name, final Class<T> clazz) throws NoSuchElementException, ClassCastException {
        final E found = find(new Predicate<E>() {
            @Override
            public boolean apply(E input) {
                return input.getName().equals(name);
            }
        });

        if (found != null)
            return clazz.cast(found);
        else
            throw new NoSuchElementException("Couldn't find " + clazz + " with name '" + name + "'. " +
                    "Possible candidates are: " + Joiner.on(", ").join(Lists.transform(this, new Function<E, String>() {
                @Override
                public String apply(E input) {
                    return input.getName();
                }
            })));
    }

    @Override
    public E find(Predicate<? super E> predicate) throws NoSuchElementException {
        return delegate().find(predicate);
    }

    @Override
    public E find(Predicate<? super E> predicate, E defaultValue) {
        return delegate().find(predicate, defaultValue);
    }

    @Override
    public abstract AbstractComponentList<E> deepClone(DeepCloner cloner);
}
