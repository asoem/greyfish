package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.Collections;

@Root
public abstract class AbstractAgentProperty<T, A extends Agent<A, ?>> extends AbstractAgentComponent<A> implements AgentProperty<A, T> {

    @Override
    public void configure(ConfigurationHandler e) {
    }

    protected AbstractAgentProperty(AbstractBuilder<? extends AbstractAgentProperty<T,A>, A, ? extends AbstractBuilder<?,A,?>> builder) {
        super(builder);
    }

    protected AbstractAgentProperty(AbstractAgentProperty<T,A> cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public AgentNode parent() {
        return getAgent();
    }

    protected static abstract class AbstractBuilder<C extends AbstractAgentProperty<?,A>, A extends Agent<A, ?>, B extends AbstractBuilder<C,A,B>> extends AbstractAgentComponent.AbstractBuilder<A,C,B>  implements Serializable {
        protected AbstractBuilder(AbstractAgentProperty<?,A> abstractAgentProperty) {
            super(abstractAgentProperty);
        }

        public AbstractBuilder() {}
    }
}
