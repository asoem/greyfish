package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public class ForwardingGene<T> implements Gene<T> {

    private Gene<T> delegate;

    /**
     * Copy Constructor
     * @param gene the delegate to forward methods to
     */
    public ForwardingGene(Gene<T> gene) {
        setDelegate(gene);
    }

    public ForwardingGene(ForwardingGene gene, DeepCloner map) {
        map.setAsCloned(gene, this);
        delegate = map.continueWith(gene.delegate, Gene.class);
    }

    @SuppressWarnings("unchecked")
    public void setDelegate(Gene<?> newDelegate) {
        if (checkNotNull(newDelegate) == this)
            throw new IllegalArgumentException("Delegation to 'this' is not allowed");
        this.delegate = Gene.class.cast(newDelegate);
    }


    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public Class<T> getSupplierClass() {
        return delegate.getSupplierClass();
    }

    @Override
    public GeneController<T> getGeneController() {
        return delegate.getGeneController();
    }

    @Override
    public boolean isMutatedCopyOf(Gene<?> gene) {
        return this.delegate.isMutatedCopyOf(gene);
    }

    public static <T> ForwardingGene<T> newInstance(Gene<T> delegate) {
        return new ForwardingGene<T>(delegate);
    }

    public Gene<T> getDelegate() {
        return delegate;
    }

    @Override
    public double distance(Gene<?> thatGene) {
        return delegate.distance(thatGene);
    }

    @Override
    public T mutated() {
        return delegate.mutated();
    }

    @Override
    public Agent getAgent() {
        return delegate.getAgent();
    }

    @Override
    public void setAgent(@Nullable Agent agent) {
        delegate.setAgent(agent);
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public boolean hasName(String s) {
        return delegate.hasName(s);
    }

    @Override
    public void checkConsistency() {
        delegate.checkConsistency();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        delegate.configure(e);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        delegate.accept(visitor);
    }

    @Override
    public void prepare(Simulation context) {
        delegate.prepare(context);
    }

    @Override
    public void freeze() {
        delegate.freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate.isFrozen();
    }

    @Override
    public void checkNotFrozen() throws IllegalStateException {
        delegate.checkNotFrozen();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Iterator<GFComponent> iterator() {
        return delegate.iterator();
    }

    @Override
    public String toString() {
        return "Gene@[" + get() + "]";
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ForwardingGene(this, cloner);
    }
}
