package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.asoem.utils.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DeepCloner clonerMock = mock(DeepCloner.class);
        final ActionCondition<DefaultGreyfishAgent> mock = mock(ActionCondition.class);
        given(clonerMock.getClone(any(ActionCondition.class), eq(ActionCondition.class))).willReturn(mock(ActionCondition.class));
        final AllCondition<DefaultGreyfishAgent> allCondition = AllCondition.evaluates(mock, mock);

        // when
        AllCondition<DefaultGreyfishAgent> clone = allCondition.deepClone(clonerMock);

        // then
        assertThat(clone, isCopyOf(allCondition));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<DefaultGreyfishAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        AllCondition<DefaultGreyfishAgent> allCondition = AllCondition.evaluates(condition);

        // when
        final AllCondition<DefaultGreyfishAgent> copy = Persisters.createCopy(allCondition, JavaPersister.INSTANCE);

        // then
        assertThat(copy, isCopyOf(allCondition));
    }

    private static <A extends Agent<A, ?>> Matcher<? super AllCondition<A>> isCopyOf(final AllCondition<A> allCondition) {
        return Matchers.<AllCondition<A>>allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AllCondition<A>, List<ActionCondition<A>>>() {
                            @Override
                            public List<ActionCondition<A>> apply(AllCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition<A>>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(is(Matchers.<ActionCondition<A>>instanceOf(ActionCondition.class))),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
