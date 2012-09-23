package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.simpleframework.xml.ElementList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class MutableComponentList<E extends AgentComponent> extends AbstractComponentList<E> {

    @ElementList(name = "components", entry = "component", inline = true, empty = false, required = false)
    private final List<E> delegate;

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    public MutableComponentList(@ElementList(name = "components", entry = "component", inline = true, empty = false, required = false) List<E> components) {
        delegate = Lists.newArrayList(components);
    }

    @SuppressWarnings("unchecked")
    public MutableComponentList(MutableComponentList<E> list, final DeepCloner cloner) {
        cloner.addClone(this);
        delegate = Lists.newArrayList(Iterables.transform(list, new Function<E, E>() {
            @Override
            public E apply(@Nullable E e) {
                return (E) cloner.getClone(e, AgentComponent.class);
            }
        }));
    }

    public MutableComponentList() {
        delegate = Lists.newArrayList();
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

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableComponentList<E>(this, cloner);
    }
}
