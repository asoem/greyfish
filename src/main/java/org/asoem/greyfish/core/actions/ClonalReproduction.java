package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.AgentTraits;
import org.asoem.greyfish.core.genes.ChromosomeImpl;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.MotionObject2DImpl;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

@Tagged("actions")
public class ClonalReproduction extends AbstractAgentAction {

    @Element(name = "nClones")
    private Callback<? super ClonalReproduction, Integer> nClones;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproduction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed() {
        final int nClones = Callbacks.call(this.nClones, this);
        for (int i = 0; i < nClones; i++) {

            agent().reproduce(new Initializer<Agent>() {
                @Override
                public void initialize(Agent initializable) {
                    initializable.setProjection(MotionObject2DImpl.copyOf(initializable.getProjection()));
                    initializable.updateGeneComponents(
                            new ChromosomeImpl(
                                    Iterables.transform(agent().getTraits(), new Function<AgentTrait<?, A>, Gene<?>>() {
                                        @Override
                                        public Gene<?> apply(@Nullable AgentTrait<?, A> gene) {
                                            assert gene != null;
                                            return new Gene<Object>(AgentTraits.mutate(gene, gene.getAllele()), gene.getRecombinationProbability());
                                        }
                                    }), Sets.newHashSet(agent().getId())));
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
    protected static abstract class AbstractBuilder<C extends ClonalReproduction, B extends AbstractBuilder<C, B>> extends AbstractAgentAction.AbstractBuilder<C, B> {
        private Callback<? super ClonalReproduction, Integer> nClones = Callbacks.constant(1);

        public B nClones(Callback<? super ClonalReproduction, Integer> nClones) {
            this.nClones = nClones;
            return self();
        }
    }
}
