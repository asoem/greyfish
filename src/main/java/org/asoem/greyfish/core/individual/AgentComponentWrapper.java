package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 12:13
 */
public class AgentComponentWrapper extends AbstractAgentComponent {

    private final Iterable<AgentComponent> children;

    AgentComponentWrapper(Iterable<AgentComponent> children) {
        super();
        this.children = checkNotNull(children);
    }

    public AgentComponentWrapper(AgentComponentWrapper agentComponentWrapper, final DeepCloner cloner) {
        super(agentComponentWrapper, cloner);
        this.children = Iterables.transform(agentComponentWrapper.children, new Function<AgentComponent, AgentComponent>() {
            @Override
            public AgentComponent apply(@Nullable AgentComponent agentComponent) {
                return cloner.cloneField(agentComponent, AgentComponent.class);
            }
        });
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new AgentComponentWrapper(this, cloner);
    }

    @Override
    public final Iterable<AgentComponent> children() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AgentComponentWrapper that = (AgentComponentWrapper) o;

        return children.equals(that.children);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + children.hashCode();
        return result;
    }
}
