package org.asoem.greyfish.core.actions;

import com.google.common.reflect.TypeToken;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.asoem.greyfish.core.conditions.AlwaysTrueCondition;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.individual.GreyfishExpressionCallback;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:34
 */
public class SexualReproductionActionTest {

    @Inject
    private Persister persister;

    @Inject
    private GreyfishExpressionFactory expressionFactory;

    public SexualReproductionActionTest() {
        Guice.createInjector(new CoreModule()).injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final AlwaysTrueCondition condition = new AlwaysTrueCondition();
        final SexualReproductionAction action = SexualReproductionAction.with()

                .name("test")
                .clutchSize(GreyfishExpressionCallback.create(expressionFactory.compile("1"), Integer.class))
                .spermSupplier(GreyfishExpressionCallback.create(expressionFactory.compile(""), new TypeToken<List<? extends Chromosome>>() {}))
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
