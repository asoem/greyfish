package org.asoem.greyfish.core.actions;

import com.google.common.collect.ForwardingList;
import org.asoem.greyfish.core.individual.ComponentList;
import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 16:01
 */
public abstract class ForwardingComponentList<E extends AgentComponent> extends ForwardingList<E> implements ComponentList<E> {

    @Override
    public <T extends E> T find(String name, Class<T> clazz) {
        return delegate().find(name, clazz);
    }

    protected abstract ComponentList<E> delegate();
}
