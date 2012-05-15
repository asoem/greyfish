package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

@ClassGroup(tags="actions")
public class ClonalReproductionAction extends AbstractGFAction {

    @Element(name = "nClones")
    private GreyfishExpression nClones;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproductionAction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {

        final int nClones = this.nClones.evaluateForContext(this).asInt();
        for (int i = 0; i < nClones; i++) {

            final Agent agent = simulation.createAgent(agent().getPopulation());
            agent.updateGeneComponents(
                    new Chromosome(new UniparentalChromosomalOrigin(agent().getId()),
                            Iterables.transform(ImmutableGeneComponentList.mutatedCopyOf(agent().getGeneComponentList()), new Function<GeneComponent<?>, Gene<?>>() {
                                @Override
                                public Gene<?> apply(@Nullable GeneComponent<?> gene) {
                                    assert gene != null;
                                    return new Gene<Object>(gene.getValue(), gene.getRecombinationProbability());
                                }
                            })));
            simulation.activateAgent(agent, agent().getProjection());

            agent().logEvent(this, "offspringProduced", "");
        }
        return ActionState.SUCCESS;
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
        @Override protected ClonalReproductionAction checkedBuild() { return new ClonalReproductionAction(this); }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ClonalReproductionAction, T extends AbstractBuilder<E,T>> extends AbstractActionBuilder<E,T> {
        private GreyfishExpression nClones = GreyfishExpressionFactoryHolder.compile("1");

        public T nClones(GreyfishExpression nClones) { this.nClones = nClones; return self(); }
    }
}
