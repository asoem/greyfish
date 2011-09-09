package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.simpleframework.xml.Element;

import java.util.Iterator;

public abstract class PropertyDecorator extends AbstractDeepCloneable implements GFProperty {

    @Element(name = "delegate")
    protected abstract GFProperty getDelegate();

    @Override
    public Iterable<Gene<?>> getGenes() {
        return getDelegate().getGenes();
    }

    @Override
    public Agent getAgent() {
        return getDelegate().getAgent();
    }

    @Override
    public void setAgent(Agent individual) {
        getDelegate().setAgent(individual);
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) {
        getDelegate().checkConsistency(components);
    }

    @Override
    public void prepare(Simulation simulation) {
        getDelegate().prepare(simulation);
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
    public void configure(ConfigurationHandler e) {
        getDelegate().configure(e);
    }

    @Override
    public void setGenes(Iterable<? extends Gene<?>> geneIterator) {
        getDelegate().setGenes(geneIterator);
    }

    @Override
    public boolean hasName(String s) {
        return getDelegate().hasName(s);
    }

    @Override
    public <S> Gene<S> registerGene(Gene<S> gene) {
        return getDelegate().registerGene(gene);
    }
}
