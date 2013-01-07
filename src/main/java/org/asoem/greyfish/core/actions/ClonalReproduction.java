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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Tagged("actions")
public class ClonalReproduction<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private Callback<? super ClonalReproduction<A>, Integer> clutchSize;
    private Callback<? super ClonalReproduction<A>, Void> offspringInitializer;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproduction() {
        this(new Builder<A>());
    }

    @Override
    protected ActionState proceed() {
        final int nClones = Callbacks.call(this.clutchSize, this);
        for (int i = 0; i < nClones; i++) {

            agent().reproduce(new Initializer<A>() {
                @Override
                public void initialize(A initializable) {
                    offspringInitializer.apply(ClonalReproduction.this, ArgumentMap.of("agent", initializable));

                    initializable.updateGeneComponents(
                            new ChromosomeImpl(
                                    Iterables.transform(agent().getTraits(), new Function<AgentTrait<A, ?>, Gene<?>>() {
                                        @Override
                                        public Gene<?> apply(@Nullable AgentTrait<A, ?> gene) {
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
    public ClonalReproduction<A> deepClone(CloneMap cloneMap) {
        return new ClonalReproduction<A>(this, cloneMap);
    }

    public ClonalReproduction(ClonalReproduction<A> cloneable, CloneMap map) {
        super(cloneable, map);
        this.clutchSize = cloneable.clutchSize;
        this.offspringInitializer = cloneable.offspringInitializer;
    }

    protected ClonalReproduction(AbstractBuilder<A, ? extends ClonalReproduction<A>, ? extends AbstractBuilder<A, ClonalReproduction<A>, ?>> builder) {
        super(builder);
        this.clutchSize = builder.nClones;
        this.offspringInitializer = builder.offspringInitializer;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("clutchSize", TypedValueModels.forField("clutchSize", this, GreyfishExpression.class));
    }

    public Callback<? super ClonalReproduction<A>, Integer> getClutchSize() {
        return clutchSize;
    }

    public void setClutchSize(Callback<? super ClonalReproduction<A>, Integer> clutchSize) {
        this.clutchSize = clutchSize;
    }

    public static <A extends Agent<A, ?>> Builder<A> with() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, ClonalReproduction<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected ClonalReproduction<A> checkedBuild() {
            return new ClonalReproduction<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends ClonalReproduction<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> {
        private Callback<? super ClonalReproduction<A>, Integer> nClones;
        private Callback<? super ClonalReproduction<A>, Void> offspringInitializer = Callbacks.emptyCallback();

        public B nClones(Callback<? super ClonalReproduction<A>, Integer> nClones) {
            this.nClones = checkNotNull(nClones);
            return self();
        }

        public B offspringInitializer(Callback<? super ClonalReproduction<A>, Void> projectionFactory) {
            this.offspringInitializer = checkNotNull(projectionFactory);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            checkState(nClones != null);
            checkState(offspringInitializer != null);
        }
    }
}
