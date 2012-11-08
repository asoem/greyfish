package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.asoem.utils.test.TransformingTypeSafeMatcher.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 05.11.12
 * Time: 09:58
 */
public class GenericConditionTest {

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DeepCloner deepCloner = mock(DeepCloner.class);
        final AgentAction action = mock(AgentAction.class);
        given(deepCloner.getClone(action, AgentAction.class)).willReturn(action);
        final ActionCondition condition = mock(ActionCondition.class);
        given(deepCloner.getClone(condition, ActionCondition.class)).willReturn(condition);
        given(condition.getAction()).willReturn(action);
        final Callback<Object, Boolean> callback = Callbacks.constant(true);

        GenericCondition genericCondition = GenericCondition.evaluate(callback);
        genericCondition.setParent(condition);

        // when
        final GenericCondition clone = genericCondition.deepClone(deepCloner);

        // then
        assertThat(clone, isSameAs(genericCondition));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final GenericCondition genericCondition = GenericCondition.evaluate(Callbacks.constant(true));
        // when
        final GenericCondition copy = Persisters.createCopy(genericCondition, JavaPersister.INSTANCE);
        // then
        assertThat(copy, isSameAs(genericCondition));
    }

    private static Matcher<? super GenericCondition> isSameAs(GenericCondition genericCondition) {
        return Matchers.<GenericCondition>allOf(
                is(not(nullValue())),
                is(not(sameInstance(genericCondition))),
                has("callback == " + genericCondition.getCallback(), new Function<GenericCondition, Callback<? super GenericCondition, Boolean>>() {
                    @Override
                    public Callback<? super GenericCondition, Boolean> apply(GenericCondition input) {
                        return input.getCallback();
                    }
                }, is(Matchers.<Callback<? super GenericCondition, Boolean>>equalTo(genericCondition.getCallback()))),
                has("action == " + genericCondition.getAction(), new Function<GenericCondition, AgentAction>() {
                    @Override
                    public AgentAction apply(GenericCondition input) {
                        return input.getAction();
                    }
                }, is(equalTo(genericCondition.getAction()))),
                has("parent == " + genericCondition.getParent(), new Function<GenericCondition, ActionCondition>() {
                    @Override
                    public ActionCondition apply(GenericCondition input) {
                        return input.getParent();
                    }
                }, is(equalTo(genericCondition.getParent())))
        );
    }
}
