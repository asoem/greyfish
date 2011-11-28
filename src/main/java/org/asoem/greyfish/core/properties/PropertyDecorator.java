package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.individual.ComponentVisitor;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

public abstract class PropertyDecorator implements GFProperty {

    @Element(name = "getVisibleScenarioEditorPane")
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
    public void setAgent(Agent agent) {
        getDelegate().setAgent(agent);
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

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return getDelegate().children();
    }
}
