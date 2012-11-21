package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.asoem.utils.test.TransformingTypeSafeMatcher.has;
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
        final ActionCondition mock = mock(ActionCondition.class);
        given(clonerMock.getClone(any(ActionCondition.class), eq(ActionCondition.class))).willReturn(mock(ActionCondition.class));
        final AllCondition<A> allCondition = AllCondition.evaluates(mock, mock);

        // when
        AllCondition<A> clone = allCondition.deepClone(clonerMock);

        // then
        assertThat(clone, isCopyOf(allCondition));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition condition = mock(ActionCondition.class, withSettings().serializable());
        AllCondition<A> allCondition = AllCondition.evaluates(condition);

        // when
        final AllCondition<A> copy = Persisters.createCopy(allCondition, JavaPersister.INSTANCE);

        // then
        assertThat(copy, isCopyOf(allCondition));
    }

    private static Matcher<? super AllCondition<A>> isCopyOf(final AllCondition<A> allCondition) {
        return Matchers.<AllCondition<A>>allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AllCondition<A>, List<ActionCondition>>() {
                            @Override
                            public List<ActionCondition> apply(AllCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(isA(ActionCondition.class)),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
