package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 12:15
 */
public abstract class AbstractGeneComponent<T> extends AbstractAgentComponent implements GeneComponent<T> {

    protected AbstractGeneComponent() {
    }

    protected AbstractGeneComponent(AbstractAgentComponent cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AbstractGeneComponent(String name) {
        super(name);
    }

    public AbstractGeneComponent(AbstractComponentBuilder<? extends AbstractGeneComponent<?>, ? extends AbstractComponentBuilder> builder) {
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
    public boolean isMutatedCopy(@Nullable GeneComponent<?> gene) {
        return gene != null && gene.getAlleleClass().equals(this.getAlleleClass());
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(createInitialValue());
    }
}
