package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Root;

import java.util.Collections;

@Root
public abstract class AbstractGFProperty<T> extends AbstractAgentComponent implements GFProperty<T> {

    @Override
    public void configure(ConfigurationHandler e) {
    }

    protected AbstractGFProperty(AbstractBuilder<? extends AbstractGFProperty, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractGFProperty(AbstractGFProperty<T> cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    protected static abstract class AbstractBuilder<E extends AbstractGFProperty, T extends AbstractBuilder<E, T>> extends AbstractComponentBuilder<E, T> {}
}
