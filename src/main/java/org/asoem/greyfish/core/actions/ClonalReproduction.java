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
import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;
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

            simulation.createAgent(agent().getPopulation(), new Initializer<Agent>() {
                @Override
                public void initialize(Agent initializable) {
                    initializable.setProjection(MotionObject2DImpl.copyOf(initializable.getProjection()));
                    initializable.updateGeneComponents(
                            new ChromosomeImpl(new UniparentalChromosomalHistory(agent().getId()),
                                    Iterables.transform(agent().getTraits(), new Function<AgentTrait<?>, Gene<?>>() {
                                        @Override
                                        public Gene<?> apply(@Nullable AgentTrait<?> gene) {
                                            assert gene != null;
                                            return new Gene<Object>(GenesComponents.mutate(gene, gene.getAllele()), gene.getRecombinationProbability());
                                        }
                                    })));
                }
            });

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

    protected ClonalReproduction(AbstractBuilder<? extends ClonalReproduction, ? extends AbstractBuilder> builder) {
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
    protected static abstract class AbstractBuilder<C extends ClonalReproduction, B extends AbstractBuilder<C, B>> extends AbstractGFAction.AbstractBuilder<C, B> {
        private Callback<? super ClonalReproduction, Integer> nClones = Callbacks.constant(1);

        public B nClones(Callback<? super ClonalReproduction, Integer> nClones) {
            this.nClones = nClones;
            return self();
        }
    }
}
