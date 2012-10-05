package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.collect.SearchableLists;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class MutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> {

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final SearchableList<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private MutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) SearchableList<E> components) {
        delegate = components;
    }

    @SuppressWarnings("unchecked")
    public MutableComponentList(MutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(list, this);
        delegate = SearchableLists.extend(Lists.newArrayList(Iterables.transform(list, new Function<E, E>() {
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.getClone(e, AgentComponent.class);
            }
        })));
    }

    public MutableComponentList() {
        delegate = SearchableLists.extend(Lists.<E>newArrayList());
    }

    public MutableComponentList(Iterable<? extends E> components) {
        delegate = SearchableLists.extend(Lists.newArrayList(components));
    }

    @Override
    protected SearchableList<E> delegate() {
        return delegate;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableComponentList<E>(this, cloner);
    }
}
