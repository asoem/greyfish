package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.09.11
 * Time: 20:05
 */
public class MutableGene<E> extends AbstractGFComponent implements Gene<E> {
    private final Class<E> supplierClass;
    private final GeneController<E> geneController;
    private E value;

    public MutableGene(E value, Class<E> supplierClass, GeneController<E> geneController) {
        this.value = checkNotNull(value);
        this.supplierClass = checkNotNull(supplierClass);
        this.geneController = checkNotNull(geneController);
    }

    public MutableGene(MutableGene<E> mutableGene, DeepCloner cloner) {
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
    public boolean isMutatedCopyOf(Gene<?> gene) {
        return this.getGeneController().equals(gene.getGeneController());
    }

    @Override
    public double distance(Gene<?> thatGene) {
        checkArgument(isMutatedCopyOf(thatGene));
        return getGeneController().normalizedDistance(this.get(), supplierClass.cast(thatGene.get()));
    }

    @Override
    public void set(E value) {
        this.value = checkNotNull(value);
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
}
