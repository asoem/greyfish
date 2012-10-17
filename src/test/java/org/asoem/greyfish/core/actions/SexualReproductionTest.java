package org.asoem.greyfish.core.actions;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.collect.ElementSelectionStrategies;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:34
 */
public class SexualReproductionTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final SexualReproduction action = SexualReproduction.builder()
                .name("test")
                .clutchSize(Callbacks.constant(1))
                .spermSupplier(Callbacks.constant(Lists.<Chromosome>newArrayList()))
                .spermSelectionStrategy(ElementSelectionStrategies.<Chromosome>randomSelection())
                .spermFitnessCallback(Callbacks.constant(0.42))
                .onSuccess(Callbacks.emptyCallback())
                .executedIf(new AlwaysTrueCondition())
                .build();

        // when
        SexualReproduction copy = Persisters.createCopy(action, SexualReproduction.class, JavaPersister.INSTANCE);

        // then
        assertThat(copy).isEqualTo(action);
    }
}
