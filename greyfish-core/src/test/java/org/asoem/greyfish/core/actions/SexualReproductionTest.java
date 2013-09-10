package org.asoem.greyfish.core.actions;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.traits.Chromosome;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:34
 */
public class SexualReproductionTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final SexualReproduction<DefaultGreyfishAgent> action = SexualReproduction.<DefaultGreyfishAgent>builder()
                .name("test")
                .clutchSize(Callbacks.constant(1))
                .spermSupplier(Callbacks.constant(Lists.<Chromosome>newArrayList()))
                .spermSelectionStrategy(ElementSelectionStrategies.<Chromosome>randomSelection())
                .spermFitnessCallback(Callbacks.constant(0.42))
                .onSuccess(Callbacks.emptyCallback())
                .executedIf(new AlwaysTrueCondition<DefaultGreyfishAgent>())
                .build();

        // when
        final SexualReproduction<DefaultGreyfishAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(action)));
    }
}
