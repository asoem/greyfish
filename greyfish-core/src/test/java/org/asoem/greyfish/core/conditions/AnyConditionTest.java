package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.asoem.greyfish.core.test.GreyfishMatchers.has;
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
        final AnyCondition<DefaultGreyfishAgent> anyCondition = AnyCondition.evaluates(condition);

        // when
        final AnyCondition<DefaultGreyfishAgent> copy = Persisters.createCopy(anyCondition, Persisters.javaSerialization());

        // then
        assertThat(copy, isCopyOf(anyCondition));
    }

    private static <A extends Agent<A, ?>> Matcher<AnyCondition<A>> isCopyOf(final AnyCondition<A> allCondition) {
        return allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AnyCondition<A>, List<ActionCondition<A>>>() {
                            @Override
                            public List<ActionCondition<A>> apply(final AnyCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition<A>>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(is(Matchers.<ActionCondition<A>>instanceOf(ActionCondition.class))),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
