package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedList;
import org.asoem.greyfish.utils.collect.AugmentedLists;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
class MutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> {

    private final AugmentedList<E> delegate;

    MutableComponentList(Iterable<? extends E> components) {
        delegate = AugmentedLists.newAugmentedArrayList(components);
    }

    private MutableComponentList(MutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(list, this);
        delegate = AugmentedLists.newAugmentedArrayList(Iterables.transform(list, new Function<E, E>() {
            @SuppressWarnings("unchecked") // casting a clone should be safe
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.getClone(e);
            }
        }));
    }

    @Override
    protected AugmentedList<E> delegate() {
        return delegate;
    }

    @Override
    public MutableComponentList<E> deepClone(DeepCloner cloner) {
        return new MutableComponentList<E>(this, cloner);
    }

    public static <E extends AgentComponent> MutableComponentList<E> copyOf(Iterable<? extends E> components) {
        return new MutableComponentList<E>(components);
    }

    private Object writeReplace() {
        return new SerializedForm<E>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm<E extends AgentComponent> implements Serializable {
        private List<E> components;

        SerializedForm(MutableComponentList<E> list) {
            this.components = new ArrayList<E>(list);
        }

        private Object writeReplace() {
            return new MutableComponentList<E>(components);
        }

        private static final long serialVersionUID = 0;
    }
}
