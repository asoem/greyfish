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
import org.asoem.greyfish.utils.space.Object2D;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Tagged("actions")
public class ClonalReproduction<A extends Agent<?,A,P>, P extends Object2D> extends AbstractAgentAction<A> {

    private Callback<? super ClonalReproduction, Integer> clutchSize;
    private Callback<? super ClonalReproduction, P> projectionFactory;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproduction() {
        this(new Builder());
    }

    @Override
    protected ActionState proceed() {
        final int nClones = Callbacks.call(this.clutchSize, this);
        for (int i = 0; i < nClones; i++) {

            agent().reproduce(new Initializer<A>() {
                @Override
                public void initialize(A initializable) {
                    //initializable.setProjection(MotionObject2DImpl.copyOf(initializable.getProjection()));
                    initializable.setProjection(projectionFactory.apply(ClonalReproduction.this, ArgumentMap.of()));
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
        this.clutchSize = cloneable.clutchSize;
        this.projectionFactory = cloneable.projectionFactory;
    }

    protected ClonalReproduction(AbstractBuilder<A, P, ? extends ClonalReproduction<A,P>, ? extends AbstractBuilder<A,P,ClonalReproduction<A,P>,?>> builder) {
        super(builder);
        this.clutchSize = builder.nClones;
        this.projectionFactory = builder.projectionFactory;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("clutchSize", TypedValueModels.forField("clutchSize", this, GreyfishExpression.class));
    }

    public Callback<? super ClonalReproduction, Integer> getClutchSize() {
        return clutchSize;
    }

    public void setClutchSize(Callback<? super ClonalReproduction, Integer> clutchSize) {
        this.clutchSize = clutchSize;
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder<A extends Agent<?, A, P>, P extends Object2D> extends AbstractBuilder<A, P, ClonalReproduction<A,P>, Builder<A,P>> {
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
    protected static abstract class AbstractBuilder<A extends Agent<?, A, P>, P extends Object2D, C extends ClonalReproduction, B extends AbstractBuilder<A, P, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> {
        private Callback<? super ClonalReproduction, Integer> nClones;
        private Callback<? super ClonalReproduction, P> projectionFactory;

        public B nClones(Callback<? super ClonalReproduction, Integer> nClones) {
            this.nClones = checkNotNull(nClones);
            return self();
        }

        public B projectionFactory(Callback<? super ClonalReproduction, P> projectionFactory) {
            this.projectionFactory = checkNotNull(projectionFactory);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(nClones != null);
            checkState(projectionFactory != null);
        }
    }
}
