package org.asoem.greyfish.core.actions.utils;

import com.google.common.collect.ForwardingList;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentList;

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
