package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import java.util.List;

import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:34
 */
public class SexualReproductionTest {

    @Inject
    private Persister persister;

    @Inject
    private GreyfishExpressionFactory expressionFactory;

    public SexualReproductionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final AlwaysTrueCondition condition = new AlwaysTrueCondition();
        final SexualReproduction action = SexualReproduction.with()

                .name("test")
                .clutchSize(constant(1))
                .spermSupplier(Callbacks.<List<? extends Chromosome>>constant(null))
                .executesIf(condition)

                .build();

        // when
        SexualReproduction deserialized = Persisters.createCopy(action, SexualReproduction.class, persister);

        // then
        assertThat(deserialized.getName()).isEqualTo("test");
        assertThat(deserialized.getClutchSize()).isEqualTo(constant(1));
        //assertThat(deserialized.getSpermStorage()).isEqualTo(storage);
        assertThat(deserialized.getCondition()).isInstanceOf(condition.getClass());
    }
}
