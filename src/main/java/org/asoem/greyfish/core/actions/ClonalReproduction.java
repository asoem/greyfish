package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.genes.*;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

@ClassGroup(tags = "actions")
public class ClonalReproduction extends AbstractGFAction {

    @Element(name = "nClones")
    private Callback<? super ClonalReproduction, Integer> nClones;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproduction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final int nClones = Callbacks.call(this.nClones, this);
        for (int i = 0; i < nClones; i++) {

            final Agent clone = simulation.createAgent(agent().getPopulation());
            clone.updateGeneComponents(
                    new ChromosomeImpl(new UniparentalChromosomalHistory(agent().getId()),
                            Iterables.transform(agent().getGeneComponentList(), new Function<GeneComponent<?>, Gene<?>>() {
                                @Override
                                public Gene<?> apply(@Nullable GeneComponent<?> gene) {
                                    assert gene != null;
                                    return new Gene<Object>(GenesComponents.mutate(gene, gene.getAllele()), gene.getRecombinationProbability());
                                }
                            })));
            simulation.activateAgent(clone, agent().getProjection());

            agent().logEvent(this, "offspringProduced", "");
        }
        return ActionState.COMPLETED;
    }

    @Override
    public ClonalReproduction deepClone(DeepCloner cloner) {
        return new ClonalReproduction(this, cloner);
    }

    public ClonalReproduction(ClonalReproduction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.nClones = cloneable.nClones;
    }

    protected ClonalReproduction(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.nClones = builder.nClones;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("nClones", TypedValueModels.forField("nClones", this, GreyfishExpression.class));
    }

    public Callback<? super ClonalReproduction, Integer> getnClones() {
        return nClones;
    }

    public void setnClones(Callback<? super ClonalReproduction, Integer> nClones) {
        this.nClones = nClones;
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<ClonalReproduction, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ClonalReproduction checkedBuild() {
            return new ClonalReproduction(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<E extends ClonalReproduction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        private Callback<? super ClonalReproduction, Integer> nClones = Callbacks.constant(1);

        public T nClones(Callback<? super ClonalReproduction, Integer> nClones) {
            this.nClones = nClones;
            return self();
        }
    }
}
