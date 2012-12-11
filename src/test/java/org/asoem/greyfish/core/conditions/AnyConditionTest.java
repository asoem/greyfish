package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.asoem.utils.test.GreyfishMatchers.has;
import static org.asoem.utils.test.GreyfishMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * User: christoph
 * Date: 02.11.12
 * Time: 14:57
 */
public class AnyConditionTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<DefaultGreyfishAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        AnyCondition<DefaultGreyfishAgent> anyCondition = AnyCondition.evaluates(condition);

        // when
        final AnyCondition<DefaultGreyfishAgent> copy = Persisters.createCopy(anyCondition, JavaPersister.INSTANCE);

        // then
        assertThat(copy, isCopyOf(anyCondition));
    }

    private static <A extends Agent<A, ?>> Matcher<AnyCondition<A>> isCopyOf(final AnyCondition<A> allCondition) {
        return Matchers.allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AnyCondition<A>, List<ActionCondition<A>>>() {
                            @Override
                            public List<ActionCondition<A>> apply(AnyCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition<A>>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(isA(new TypeToken<ActionCondition<A>>() {})),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
