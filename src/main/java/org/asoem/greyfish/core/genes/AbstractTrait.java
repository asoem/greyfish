package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 12:15
 */
public abstract class AbstractTrait<A extends Agent<A, ?>, T> extends AbstractAgentComponent<A> implements AgentTrait<A, T> {

    protected AbstractTrait() {}

    protected AbstractTrait(AbstractAgentComponent<A> cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    public AbstractTrait(AbstractBuilder<A, ? extends AbstractTrait<A, T>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + ":" + String.valueOf(getValue()) + "]";
    }

    @Override
    public double getRecombinationProbability() {
        return 0.5;
    }

    @Override
    public Iterable<AgentNode> childConditions() {
        return Collections.emptyList();
    }

    @Override
    public AgentNode parent() {
        return getAgent();
    }

    @Override
    public boolean isMutatedCopy(@Nullable AgentTrait<A, ?> gene) {
        return gene != null && gene.getValueClass().equals(this.getValueClass());
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(createInitialValue());
    }
}
