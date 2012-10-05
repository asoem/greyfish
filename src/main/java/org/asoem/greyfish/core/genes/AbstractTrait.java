package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 12:15
 */
public abstract class AbstractTrait<T> extends AbstractAgentComponent implements AgentTrait<T> {

    protected AbstractTrait() {
    }

    protected AbstractTrait(AbstractAgentComponent cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AbstractTrait(String name) {
        super(name);
    }

    public AbstractTrait(AbstractBuilder<? extends AbstractTrait<?>, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + ":" + String.valueOf(getAllele()) + "]";
    }

    @Override
    public double getRecombinationProbability() {
        return 0.5;
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    @Override
    public boolean isMutatedCopy(@Nullable AgentTrait<?> gene) {
        return gene != null && gene.getAlleleClass().equals(this.getAlleleClass());
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(createInitialValue());
    }
}
