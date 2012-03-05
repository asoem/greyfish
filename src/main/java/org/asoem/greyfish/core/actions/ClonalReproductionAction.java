package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.ImmutableChromosome;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.Location2D;
import org.simpleframework.xml.Element;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

    @Element(name = "nClones")
    private GreyfishExpression nClones;

    @SimpleXMLConstructor
    public ClonalReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        final Location2D locatable = agent().getProjection();
        final Population population = agent().getPopulation();

        for (int i = 0; i < nClones.evaluateForContext(this).asInt(); i++) {
            final ImmutableChromosome<Gene<?>> gamete = ImmutableChromosome.mutatedCopyOf(agent().getChromosome());
            simulation.createAgent(population, gamete, locatable);

            agent().logEvent(this, "offspringProduced", "");
        }
        return ActionState.END_SUCCESS;
    }

    @Override
    public ClonalReproductionAction deepClone(DeepCloner cloner) {
        return new ClonalReproductionAction(this, cloner);
    }

    public ClonalReproductionAction(ClonalReproductionAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.nClones = cloneable.nClones;
    }

    protected ClonalReproductionAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.nClones = builder.nClones;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("nClones", TypedValueModels.forField("nClones", this, GreyfishExpression.class));
    }

    public GreyfishExpression getnClones() {
        return nClones;
    }

    public void setnClones(GreyfishExpression nClones) {
        this.nClones = nClones;
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<ClonalReproductionAction,Builder>  {
        @Override protected Builder self() { return this; }
        @Override public ClonalReproductionAction checkedBuild() { return new ClonalReproductionAction(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ClonalReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private GreyfishExpression nClones = GreyfishExpressionFactoryHolder.compile("1");

        public T nClones(GreyfishExpression nClones) { this.nClones = nClones; return self(); }
    }
}
