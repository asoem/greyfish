package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.IndexedGene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.simpleframework.xml.Element;

import java.util.Iterator;
import java.util.ListIterator;

public abstract class PropertyDecorator extends AbstractDeepCloneable implements GFProperty {

    @Element(name = "delegate")
    protected abstract GFProperty getDelegate();

    @Override
    public Iterable<IndexedGene<?>> getGenes() {
        return getDelegate().getGenes();
    }

    @Override
    public IndividualInterface getComponentOwner() {
        return getDelegate().getComponentOwner();
    }

    @Override
    public void setComponentRoot(IndividualInterface individual) {
        getDelegate().setComponentRoot(individual);
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        getDelegate().checkConsistency(components);
    }

    @Override
    public void initialize(Simulation simulation) {
        getDelegate().initialize(simulation);
    }

    @Override
    public void freeze() {
        getDelegate().freeze();
    }

    @Override
    public boolean isFrozen() {
        return getDelegate().isFrozen();
    }

    @Override
    public <T> T checkFrozen(T value) throws IllegalStateException {
        return getDelegate().checkFrozen(value);
    }

    @Override
    public void checkNotFrozen() throws IllegalStateException {
        getDelegate().checkNotFrozen();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public void setName(String name) {
        getDelegate().setName(name);
    }

    @Override
    public Iterator<GFComponent> iterator() {
        return getDelegate().iterator();
    }

    @Override
    public void export(Exporter e) {
        getDelegate().export(e);
    }

    @Override
    public void setGenes(ListIterator<? extends Gene<?>> geneIterator) {
        getDelegate().setGenes(geneIterator);
    }

    @Override
    public boolean hasName(String s) {
        return getDelegate().hasName(s);
    }

    @Override
    public <S> Supplier<S> registerGene(Gene<S> gene) {
        return getDelegate().registerGene(gene);
    }
}
