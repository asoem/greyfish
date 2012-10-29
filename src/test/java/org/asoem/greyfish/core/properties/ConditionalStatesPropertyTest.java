package org.asoem.greyfish.core.properties;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:15
 */
public class ConditionalStatesPropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        ConditionalStatesProperty statesProperty = ConditionalStatesProperty.with().addState("A", "true").build();
        
        // when
        ConditionalStatesProperty persistent = Persisters.createCopy(statesProperty, JavaPersister.INSTANCE);

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
