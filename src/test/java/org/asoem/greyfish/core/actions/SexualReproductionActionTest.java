package org.asoem.greyfish.core.actions;

import com.google.inject.Inject;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.impl.EvaluatorFake;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
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
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final GreyfishExpression fakeExpression = new GreyfishExpression("42.0", new EvaluatorFake());
        final EvaluatedGenomeStorage storage = new EvaluatedGenomeStorage();
        final AlwaysTrueCondition condition = new AlwaysTrueCondition();
        final SexualReproductionAction action = SexualReproductionAction.with()

                .name("test")
                .clutchSize(fakeExpression)
                .spermStorage(storage)
                .executesIf(condition)

                .build();

        // when
        SexualReproductionAction deserialized = Persisters.createCopy(action, SexualReproductionAction.class, persister);

        // then
        assertThat(deserialized.getName()).isEqualTo("test");
        assertThat(deserialized.getClutchSize()).isEqualTo(fakeExpression);
        assertThat(deserialized.getSpermStorage()).isEqualTo(storage);
        assertThat(deserialized.getRootCondition()).isInstanceOf(condition.getClass());
    }
}
