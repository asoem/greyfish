package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 12:15
 */
public abstract class AbstractGene<T> extends AbstractAgentComponent implements Gene<T> {

    protected AbstractGene() {
    }

    protected AbstractGene(AbstractAgentComponent cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AbstractGene(String name) {
        super(name);
    }

    public AbstractGene(AbstractBuilder<? extends AbstractGene<?>, ? extends AbstractBuilder> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + ":" + String.valueOf(get()) + "]";
    }

    @Override
    public final double distance(Gene<?> thatGene) {
        checkNotNull(thatGene);
        checkArgument(this.getSupplierClass().equals(thatGene.getSupplierClass()));
        return getGeneController().normalizedDistance(this.get(), getSupplierClass().cast(thatGene.get()));
    }

    @Override
    public double getRecombinationProbability() {
        return 0.5;
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

    @Override
    public boolean isMutatedCopy(Gene<?> gene) {
        checkNotNull(gene);
        return this.getGeneController().equals(gene.getGeneController());
    }
}
