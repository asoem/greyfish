package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Product2;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    public final double distance(GeneComponent<?> thatGene) {
        checkNotNull(thatGene);
        checkArgument(this.getSupplierClass().equals(thatGene.getSupplierClass()));
        return getGeneController().normalizedDistance(this.getAllele(), getSupplierClass().cast(thatGene.getAllele()));
    }

    @Override
    public double getRecombinationProbability() {
        return 0.5;
    }

    @Override
    public T mutatedValue() {
        return getGeneController().mutate(getAllele());
    }

    @Override
    public Product2<T, T> recombinedValue(T other) {
        return getGeneController().recombine(getAllele(), other);
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
    public boolean isMutatedCopy(@Nullable GeneComponent<?> gene) {
        return gene != null && gene.getSupplierClass().equals(this.getSupplierClass());
    }

    @Override
    public void initialize() {
        super.initialize();
        setAllele(getGeneController().createInitialValue());
    }
}
