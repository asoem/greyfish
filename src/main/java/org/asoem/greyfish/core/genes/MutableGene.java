package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.09.11
 * Time: 20:05
 */
public class MutableGene<E> extends AbstractGene<E> {
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
    public final Class<E> getSupplierClass() {
        return this.supplierClass;
    }

    @Override
    public final GeneController<E> getGeneController() {
        return this.geneController;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new MutableGene<E>(this, cloner);
    }

    @Override
    public final E get() {
        return value;
    }
}
