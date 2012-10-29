package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Collections;

@Root
public abstract class AbstractAgentProperty<T> extends AbstractAgentComponent implements AgentProperty<T> {

    @Override
    public void configure(ConfigurationHandler e) {
    }

    protected AbstractAgentProperty(AbstractBuilder<? extends AbstractAgentProperty, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected AbstractAgentProperty(AbstractAgentProperty<T> cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    protected static abstract class AbstractBuilder<C extends AbstractAgentProperty, B extends AbstractBuilder<C, B>> extends AbstractAgentComponent.AbstractBuilder<C, B>  implements Serializable {
        public AbstractBuilder(AbstractAgentProperty<?> simulationStepProperty) {
            super(simulationStepProperty);
        }

        public AbstractBuilder() {}
    }
}
