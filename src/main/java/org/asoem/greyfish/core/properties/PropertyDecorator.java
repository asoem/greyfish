package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.IndividualInterface;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;

import java.util.Iterator;
import java.util.List;

public abstract class PropertyDecorator extends AbstractDeepCloneable implements GFProperty {

    protected abstract GFProperty getDelegate();

    @Override
    public void mutate() {
        getDelegate().mutate();
    }

    @Override
    public List<? extends Gene<?>> getGeneList() {
        return getDelegate().getGeneList();
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
    public void setGenes(Iterator<Gene<?>> geneIterator) {
        getDelegate().setGenes(geneIterator);
    }
}
