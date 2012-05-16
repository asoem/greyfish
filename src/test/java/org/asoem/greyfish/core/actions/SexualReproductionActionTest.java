package org.asoem.greyfish.core.actions;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:34
 */
public class SexualReproductionActionTest {

    @Inject
    private Persister persister;

    public SexualReproductionActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final EvaluatedGenomeStorage storage = new EvaluatedGenomeStorage();
        final AlwaysTrueCondition condition = new AlwaysTrueCondition();
        final SexualReproductionAction action = SexualReproductionAction.with()

                .name("test")
                .clutchSize(Callbacks.constant(1))
                //.spermSupplier(storage)
                .executesIf(condition)

                .build();

        // when
        SexualReproductionAction deserialized = Persisters.createCopy(action, SexualReproductionAction.class, persister);

        // then
        assertThat(deserialized.getName()).isEqualTo("test");
        assertThat(deserialized.getClutchSize()).isEqualTo(Callbacks.constant(1));
        //assertThat(deserialized.getSpermStorage()).isEqualTo(storage);
        assertThat(deserialized.getCondition()).isInstanceOf(condition.getClass());
    }
}
