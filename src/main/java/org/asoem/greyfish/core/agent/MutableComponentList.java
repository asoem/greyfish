package org.asoem.greyfish.core.agent;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.collect.SearchableLists;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class MutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> {

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final SearchableList<E> delegate;

    private MutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) SearchableList<E> components) {
        delegate = components;
    }

    private MutableComponentList(MutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(list, this);
        delegate = SearchableLists.newSearchableArrayList(Iterables.transform(list, new Function<E, E>() {
            @SuppressWarnings("unchecked") // casting a clone should be safe
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.getClone(e);
            }
        }));
    }

    @Override
    protected SearchableList<E> delegate() {
        return delegate;
    }

    @Override
    public MutableComponentList deepClone(DeepCloner cloner) {
        return new MutableComponentList<E>(this, cloner);
    }

    public static <E extends AgentComponent> MutableComponentList<E> copyOf(Iterable<? extends E> components) {
        return new MutableComponentList<E>(SearchableLists.newSearchableArrayList(components));
    }

    private Object writeReplace() {
        return new SerializedForm(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializedForm implements Serializable {
        private SearchableList<? extends AgentComponent> list;

        SerializedForm(MutableComponentList<?> list) {
            this.list = list.delegate;
        }

        private Object writeReplace() {
            return MutableComponentList.copyOf(list);
        }

        private static final long serialVersionUID = 0;
    }
}
