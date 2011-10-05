package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 12:13
 */
public class RootComponent extends AbstractAgentComponent {

    RootComponent(Agent agent) {
        super();
        setAgent(checkNotNull(agent));
    }

    public RootComponent(RootComponent rootComponent, DeepCloner cloner) {
        super(rootComponent, cloner);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new RootComponent(this, cloner);
    }

    @Override
    public final Iterable<AgentComponent> children() {
        return Iterables.concat(
                Collections.singletonList(agent.get().getBody()),
                agent.get().getActions(),
                agent.get().getProperties(),
                agent.get().getGenes());
    }
}
