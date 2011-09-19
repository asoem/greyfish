package org.asoem.greyfish.core.individual;

import com.google.common.collect.Lists;
import org.asoem.greyfish.utils.HookedForwardingList;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.09.11
 * Time: 15:53
 */
public class ComponentList<T extends GFComponent> extends HookedForwardingList<T> {

    private final Agent agent;
    private final List<T> delegate;

    public ComponentList(Agent agent) {
        delegate = Lists.newArrayList();
        this.agent = agent;
    }

    @Override
    protected void beforeAddition(@Nullable T element) {
        checkNotNull(element);
    }

    @Override
    protected void afterAddition(@Nullable T element) {
        assert element != null;
        element.setAgent(agent);
    }

    @Override
    protected void beforeRemoval(@Nullable T element) {
        checkNotNull(element);
    }

    @Override
    protected void afterRemoval(@Nullable T element) {
        assert element != null;
        element.setAgent(null);
    }

    @Override
    protected void beforeReplacement(@Nullable T oldElement, @Nullable T newElement) {
        checkNotNull(newElement);
    }

    @Override
    protected void afterReplacement(@Nullable T oldElement, @Nullable T newElement) {
        assert oldElement != null;
        oldElement.setAgent(null);
        assert newElement != null;
        newElement.setAgent(agent);
    }

    @Override
    protected List<T> delegate() {
        return delegate;
    }

    public static <T extends GFComponent> ComponentList<T> forOwner(Agent agent) {
        return new ComponentList<T>(agent);
    }
}
