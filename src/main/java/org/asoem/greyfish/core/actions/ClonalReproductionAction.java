package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.io.AgentEvent;
import org.asoem.greyfish.core.io.AgentEventLogger;
import org.asoem.greyfish.core.io.AgentEventLoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.space.Coordinates2D;
import org.simpleframework.xml.Attribute;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

    private static final AgentEventLogger AGENT_EVENT_LOGGER = AgentEventLoggerFactory.getLogger();

    @Attribute(name = "nOffspring")
    private int nOffspring;

    @SimpleXMLConstructor
    public ClonalReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        final Coordinates2D coordinates = simulation.getSpace().getCoordinates(agent());
        final Population population = agent().getPopulation();

        for (int i = 0; i < nOffspring; i++) {
            final ImmutableGenome<Gene<?>> gamete = ImmutableGenome.mutatedCopyOf(agent().getGenes());
            simulation.createAgent(population, gamete, coordinates);

            AGENT_EVENT_LOGGER.addEvent(new AgentEvent(simulation, simulation.getSteps(), agent(), this, "offspringProduced", "", coordinates));
        }
        return ActionState.END_SUCCESS;
    }

    @Override
    public ClonalReproductionAction deepClone(DeepCloner cloner) {
        return new ClonalReproductionAction(this, cloner);
    }

    public ClonalReproductionAction(ClonalReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.nOffspring = cloneable.nOffspring;
    }

    protected ClonalReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.nOffspring = builder.nOffspring;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("#clones", new AbstractTypedValueModel<Integer>() {
            @Override
            protected void set(Integer arg0) {
                nOffspring = arg0;
            }

            @Override
            public Integer get() {
                return nOffspring;
            }
        });
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ClonalReproductionAction,Builder>  {
        @Override protected Builder self() { return this; }
        @Override public ClonalReproductionAction checkedBuild() { return new ClonalReproductionAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends ClonalReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private int nOffspring = 1;

        public T nOffspring(int parameterClones) { this.nOffspring = parameterClones; return self(); }
    }
}
