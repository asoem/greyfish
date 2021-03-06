/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.core.actions;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.core.agent.RequestAllTraitValues;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.core.traits.HeritableTraitsChromosome;
import org.asoem.greyfish.core.traits.Mutate;
import org.asoem.greyfish.core.traits.TraitVector;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;

import javax.annotation.Nullable;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public abstract class ClonalReproduction<A extends Agent<? extends BasicContext<?, A>>,
        C extends AgentContext<A>> extends BaseAgentAction<A, C> {

    private Callback<? super ClonalReproduction<A, C>, Integer> clutchSize;

    @Override
    protected final ActionState proceed(final C context) {
        final int nClones = Callbacks.call(this.clutchSize, this);
        for (int i = 0; i < nClones; i++) {

            final A agent = context.agent();
            Map<String, ?> traitValues = agent.ask(new RequestAllTraitValues(), Map.class);

            final Iterable<TraitVector<?>> traitVectors = Iterables.transform(traitValues.entrySet(),
                    new Function<Map.Entry<String, ?>, TraitVector<?>>() {
                        @Nullable
                        @Override
                        public TraitVector<?> apply(@Nullable final Map.Entry<String, ?> input) {
                            Object value = input.getValue();
                            return TraitVector.of(
                                    input.getKey(), agent.ask(new Mutate(input.getKey(), value), Object.class)
                            );
                        }
                    });

            final HeritableTraitsChromosome chromosome =
                    new HeritableTraitsChromosome(traitVectors);

            //agent.reproduce(chromosome);

            addAgent(chromosome);

            //agent.logEvent(this, "offspringProduced", "");
        }
        return ActionState.COMPLETED;
    }

    protected abstract void addAgent(final Chromosome chromosome);

    protected ClonalReproduction(final AbstractBuilder<A, ? extends ClonalReproduction<A, C>,
            ? extends AbstractBuilder<A, ClonalReproduction<A, C>, ?, C>, C> builder) {
        super(builder);
        this.clutchSize = builder.nClones;
    }

    public final Callback<? super ClonalReproduction<A, C>, Integer> getClutchSize() {
        return clutchSize;
    }

    public final void setClutchSize(final Callback<? super ClonalReproduction<A, C>, Integer> clutchSize) {
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
    private abstract static class AbstractBuilder<
            A extends Agent<? extends BasicContext<?, A>>,
            C extends ClonalReproduction<A, AC>,
            B extends AbstractBuilder<A, C, B, AC>,
            AC extends AgentContext<A>>
            extends BaseAgentAction.AbstractBuilder<A, C, B, AC> {
        private Callback<? super ClonalReproduction<A, AC>, Integer> nClones;
        private Callback<? super ClonalReproduction<A, AC>, Void> offspringInitializer = Callbacks.emptyCallback();

        protected AbstractBuilder() {
            addVerification(new Verification() {
                @Override
                protected void verify() {
                    checkState(nClones != null);
                    checkState(offspringInitializer != null);
                }
            });
        }

        public B nClones(final int n) {
            checkArgument(n >= 0);
            return nClones(Callbacks.constant(n));
        }

        public B nClones(final Callback<? super ClonalReproduction<A, AC>, Integer> nClones) {
            this.nClones = checkNotNull(nClones);
            return self();
        }

        public B offspringInitializer(final Callback<? super ClonalReproduction<A, AC>, Void> projectionFactory) {
            this.offspringInitializer = checkNotNull(projectionFactory);
            return self();
        }
    }
}
