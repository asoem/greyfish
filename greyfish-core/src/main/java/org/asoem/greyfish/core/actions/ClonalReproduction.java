package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.traits.AgentTrait;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.core.traits.HeritableTraitsChromosome;
import org.asoem.greyfish.core.traits.TraitVector;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.*;

public abstract class ClonalReproduction<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends BaseAgentAction<A> {

    private Callback<? super ClonalReproduction<A>, Integer> clutchSize;

    @Override
    protected ActionState proceed(final ExecutionContext<A> context) {
        final int nClones = Callbacks.call(this.clutchSize, this);
        for (int i = 0; i < nClones; i++) {

            final Iterable<TraitVector<?>> traitVectors = Iterables.transform(context.agent().getTraits(),
                    new Function<AgentTrait<A, ?>, TraitVector<?>>() {
                        @Override
                        public TraitVector<?> apply(@Nullable final AgentTrait<A, ?> trait) {
                            assert trait != null;
                            return mutatedVector(trait);
                        }
                    });

            final HeritableTraitsChromosome chromosome =
                    new HeritableTraitsChromosome(traitVectors, Sets.newHashSet(context.agent().getContext().get().getAgentId()));

            //agent.reproduce(chromosome);

            addAgent(chromosome);

            //agent.logEvent(this, "offspringProduced", "");
        }
        return ActionState.COMPLETED;
    }

    protected abstract void addAgent(final Chromosome chromosome);

    private static <T> TraitVector<T> mutatedVector(final AgentTrait<?, T> trait) {
        return TraitVector.create(
                trait.mutate(trait.get()),
                trait.getValueType(),
                trait.getName());
    }

    protected ClonalReproduction(final AbstractBuilder<A, ? extends ClonalReproduction<A>,
            ? extends AbstractBuilder<A, ClonalReproduction<A>, ?>> builder) {
        super(builder);
        this.clutchSize = builder.nClones;
    }

    public Callback<? super ClonalReproduction<A>, Integer> getClutchSize() {
        return clutchSize;
    }

    public void setClutchSize(final Callback<? super ClonalReproduction<A>, Integer> clutchSize) {
        this.clutchSize = clutchSize;
    }

    /*
     * public static <A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Builder<A> with() { return new
     * Builder<A>(); }
     * <p/>
     * public static final class Builder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends
     * ClonalReproduction.AbstractBuilder<A, ClonalReproduction<A>, Builder<A>> {
     *
     * @Override protected Builder<A> self() { return this; }
     * @Override protected ClonalReproduction<A> checkedBuild() { return new ClonalReproduction<A>(this); } }
     */
    private abstract static class AbstractBuilder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>, C extends ClonalReproduction<A>, B extends AbstractBuilder<A, C, B>> extends BaseAgentAction.AbstractBuilder<A, C, B> {
        private Callback<? super ClonalReproduction<A>, Integer> nClones;
        private Callback<? super ClonalReproduction<A>, Void> offspringInitializer = Callbacks.emptyCallback();

        public B nClones(final int n) {
            checkArgument(n >= 0);
            return nClones(Callbacks.constant(n));
        }

        public B nClones(final Callback<? super ClonalReproduction<A>, Integer> nClones) {
            this.nClones = checkNotNull(nClones);
            return self();
        }

        public B offspringInitializer(final Callback<? super ClonalReproduction<A>, Void> projectionFactory) {
            this.offspringInitializer = checkNotNull(projectionFactory);
            return self();
        }

        @Override
        protected void checkBuilder() {
            checkState(nClones != null);
            checkState(offspringInitializer != null);
        }
    }
}
