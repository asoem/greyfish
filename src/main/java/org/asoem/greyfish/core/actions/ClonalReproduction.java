package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.ChromosomeImpl;
import org.asoem.greyfish.core.genes.TraitVector;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.*;

@Tagged("actions")
public class ClonalReproduction<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private Callback<? super ClonalReproduction<A>, Integer> clutchSize;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ClonalReproduction() {
        this(new Builder<A>());
    }

    @Override
    protected ActionState proceed() {
        final int nClones = Callbacks.call(this.clutchSize, this);
        for (int i = 0; i < nClones; i++) {

            final Iterable<TraitVector<?>> traitVectors = Iterables.transform(agent().getTraits(), new Function<AgentTrait<A, ?>, TraitVector<?>>() {
                @Override
                public TraitVector<?> apply(@Nullable AgentTrait<A, ?> trait) {
                    assert trait != null;
                    return mutatedVector(trait);
                }
            });

            final ChromosomeImpl chromosome = new ChromosomeImpl(traitVectors, Sets.newHashSet(agent().getId()));

            agent().reproduce(chromosome);

            agent().logEvent(this, "offspringProduced", "");
        }
        return ActionState.COMPLETED;
    }

    private static <T> TraitVector<T> mutatedVector(AgentTrait<?, T> trait) {
        return TraitVector.create(trait.mutate(trait.get()), trait.getRecombinationProbability(), trait.getValueType());
    }

    @Override
    public ClonalReproduction<A> deepClone(DeepCloner cloner) {
        return new ClonalReproduction<A>(this, cloner);
    }

    public ClonalReproduction(ClonalReproduction<A> cloneable, DeepCloner map) {
        super(cloneable, map);
        this.clutchSize = cloneable.clutchSize;
    }

    protected ClonalReproduction(AbstractBuilder<A, ? extends ClonalReproduction<A>, ? extends AbstractBuilder<A, ClonalReproduction<A>, ?>> builder) {
        super(builder);
        this.clutchSize = builder.nClones;
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

        public B nClones(int n) {
            checkArgument(n >= 0);
            return nClones(Callbacks.constant(n));
        }

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
