package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.asoem.utils.test.GreyfishMatchers.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: christoph
 * Date: 05.11.12
 * Time: 09:58
 */
public class GenericConditionTest {

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DeepCloner deepCloner = DeepCloner.newInstance();

        final AgentAction<DefaultGreyfishAgent> action = when(mock(AgentAction.class).deepClone(deepCloner)).thenReturn(mock(AgentAction.class)).getMock();

        final ActionCondition<DefaultGreyfishAgent> condition = when(mock(ActionCondition.class).deepClone(deepCloner)).thenReturn(mock(ActionCondition.class)).getMock();
        given(condition.getAction()).willReturn(action);

        final Callback<Object, Boolean> callback = Callbacks.constant(true);

        final GenericCondition<DefaultGreyfishAgent> genericCondition = GenericCondition.evaluate(callback);
        genericCondition.setParent(condition);

        // when
        final GenericCondition<DefaultGreyfishAgent> clone = genericCondition.deepClone(deepCloner);

        // then
        assertThat(clone, isSameAs(genericCondition));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final GenericCondition<DefaultGreyfishAgent> genericCondition = GenericCondition.evaluate(Callbacks.constant(true));
        // when
        final GenericCondition<DefaultGreyfishAgent> copy = Persisters.createCopy(genericCondition, JavaPersister.INSTANCE);
        // then
        assertThat(copy, isSameAs(genericCondition));
    }

    private static Matcher<? super GenericCondition<DefaultGreyfishAgent>> isSameAs(GenericCondition<DefaultGreyfishAgent> genericCondition) {
        return Matchers.<GenericCondition<DefaultGreyfishAgent>>allOf(
                is(not(nullValue())),
                is(not(sameInstance(genericCondition))),
                has("callback == " + genericCondition.getCallback(), new Function<GenericCondition<DefaultGreyfishAgent>, Callback<? super GenericCondition<DefaultGreyfishAgent>, Boolean>>() {
                    @Override
                    public Callback<? super GenericCondition<DefaultGreyfishAgent>, Boolean> apply(GenericCondition<DefaultGreyfishAgent> input) {
                        return input.getCallback();
                    }
                }, is(Matchers.<Callback<? super GenericCondition<DefaultGreyfishAgent>, Boolean>>equalTo(genericCondition.getCallback()))),
                has("action ~= " + genericCondition.getAction(), new Function<GenericCondition<DefaultGreyfishAgent>, AgentAction<DefaultGreyfishAgent>>() {
                    @Override
                    public AgentAction<DefaultGreyfishAgent> apply(GenericCondition<DefaultGreyfishAgent> input) {
                        return input.getAction();
                    }
                }, is(genericCondition.getAction() == null ? nullValue() : instanceOf(AgentAction.class))),
                has("parent ~= " + genericCondition.getParent(), new Function<GenericCondition<DefaultGreyfishAgent>, ActionCondition<DefaultGreyfishAgent>>() {
                    @Override
                    public ActionCondition<DefaultGreyfishAgent> apply(GenericCondition<DefaultGreyfishAgent> input) {
                        return input.getParent();
                    }
                }, is(genericCondition.getParent() == null ? nullValue() : instanceOf(ActionCondition.class)))
        );
    }
}
