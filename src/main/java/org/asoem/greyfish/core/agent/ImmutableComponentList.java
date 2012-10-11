package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.AugmentedList;
import org.asoem.greyfish.utils.collect.AugmentedLists;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a immutable {@link ComponentList} implementation.
 * It cannot be subclassed and does not permit null elements.
 *
 */
public class ImmutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> implements Serializable {

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final AugmentedList<E> listDelegate;

    private ImmutableComponentList(
            @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) AugmentedList<E> components) {
        listDelegate = components;
    }

    private ImmutableComponentList(ImmutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(list, this);
        listDelegate = AugmentedLists.transform(list.listDelegate, new Function<E, E>() {
            @Override
            @SuppressWarnings("unchecked")
            public E apply(@Nullable E input) {
                return (E) cloner.getClone(input);
            }
        });
    }

    @Override
    protected AugmentedList<E> delegate() {
        return listDelegate;
    }

    @Override
    public ImmutableComponentList<E> deepClone(DeepCloner cloner) {
        return new ImmutableComponentList<E>(this, cloner);
    }

    public static <E extends AgentComponent> ImmutableComponentList<E> copyOf(Iterable<E> components) {
        checkNotNull(components);
        if (Iterables.size(components) == 0)
            return of();
        else
            return new ImmutableComponentList<E>(AugmentedLists.copyOf(components));
    }

    private static final ImmutableComponentList<AgentComponent> EMPTY_COMPONENT_LIST =
            new ImmutableComponentList<AgentComponent>(AugmentedLists.<AgentComponent>of());

    @SuppressWarnings("unchecked")
    public static <E extends AgentComponent> ImmutableComponentList<E> of() {
        return (ImmutableComponentList<E>) EMPTY_COMPONENT_LIST;
    }

    private Object writeReplace() {
        return new SerializedForm(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm implements Serializable {
        private AgentComponent[] components;

        SerializedForm(ImmutableComponentList<? extends AgentComponent> list) {
            this.components = list.toArray(new AgentComponent[list.size()]);
        }

        private Object readResolve() {
            return ImmutableComponentList.copyOf(Arrays.asList(components));
        }

        private static final long serialVersionUID = 0;
    }
}
