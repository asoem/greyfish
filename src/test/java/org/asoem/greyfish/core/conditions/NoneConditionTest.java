package org.asoem.greyfish.core.conditions;

import com.google.common.base.Function;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.asoem.utils.test.TransformingTypeSafeMatcher.has;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * User: christoph
 * Date: 02.11.12
 * Time: 14:59
 */
public class NoneConditionTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition condition = mock(ActionCondition.class, withSettings().serializable());
        NoneCondition<A> noneCondition = NoneCondition.evaluates(condition);

        // when
        final NoneCondition<A> copy = Persisters.createCopy(noneCondition, JavaPersister.INSTANCE);

        // then
        assertThat(copy, isCopyOf(noneCondition));
    }

    private static Matcher<? super NoneCondition<A>> isCopyOf(final NoneCondition<A> allCondition) {
        return Matchers.<NoneCondition<A>>allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<NoneCondition<A>, List<ActionCondition>>() {
                            @Override
                            public List<ActionCondition> apply(NoneCondition<A> input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(isA(ActionCondition.class)),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
