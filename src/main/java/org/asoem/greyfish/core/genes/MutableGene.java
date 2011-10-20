package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.09.11
 * Time: 20:05
 */
public class MutableGene<E> extends AbstractAgentComponent implements Gene<E> {
    private final Class<E> supplierClass;
    private final GeneController<E> geneController;
    private E value;

    public MutableGene(E value, Class<E> supplierClass, GeneController<E> geneController) {
        this.value = checkNotNull(value);
        this.supplierClass = checkNotNull(supplierClass);
        this.geneController = checkNotNull(geneController);
    }

    protected MutableGene(MutableGene<E> mutableGene, DeepCloner cloner) {
        super(mutableGene, cloner);
        this.supplierClass = mutableGene.supplierClass;
        this.geneController = mutableGene.geneController;
    }

    @Override
    public Class<E> getSupplierClass() {
        return this.supplierClass;
    }

    @Override
    public GeneController<E> getGeneController() {
        return this.geneController;
    }

    @Override
    public boolean isMutatedCopy(Gene<?> gene) {
        return this.getGeneController().equals(gene.getGeneController());
    }

    @Override
    public double distance(Gene<?> thatGene) {
        checkArgument(isMutatedCopy(thatGene));
        return getGeneController().normalizedDistance(this.get(), supplierClass.cast(thatGene.get()));
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGene<E>(this, cloner);
    }

    @Override
    public E get() {
        return value;
    }

    @Override
    public void set(E value) {
        this.value = checkNotNull(value);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }
}
