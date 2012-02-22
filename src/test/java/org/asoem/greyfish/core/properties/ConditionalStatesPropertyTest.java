package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import javax.annotation.Nullable;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:15
 */
public class ConditionalStatesPropertyTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public ConditionalStatesPropertyTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }
    @Test
    public void testPersistence() throws Exception {
        // given
        ConditionalStatesProperty statesProperty = ConditionalStatesProperty.with().addState("A", "true").build();
        
        // when
        ConditionalStatesProperty persistent = Persisters.createCopy(statesProperty, ConditionalStatesProperty.class, persister);

        // then
        final Map<String,GreyfishExpression> conditionMap = persistent.getConditionMap();
        assertThat(conditionMap).hasSize(1);
        assertThat(conditionMap.keySet()).containsOnly("A");
        assertThat(Iterables.transform(conditionMap.values(), new Function<GreyfishExpression, String>() {
            @Override
            public String apply(@Nullable GreyfishExpression greyfishExpression) {
                assert greyfishExpression != null;
                return greyfishExpression.getExpression();
            }
        })).containsOnly("true");
    }
}
