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
 * Time: 14:57
 */
public class AnyConditionTest {

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition condition = mock(ActionCondition.class, withSettings().serializable());
        AnyCondition anyCondition = AnyCondition.evaluates(condition);

        // when
        final AnyCondition copy = Persisters.createCopy(anyCondition, JavaPersister.INSTANCE);

        // then
        assertThat(copy, isCopyOf(anyCondition));
    }

    private static Matcher<? super AnyCondition> isCopyOf(final AnyCondition allCondition) {
        return Matchers.<AnyCondition>allOf(
                is(not(sameInstance(allCondition))),
                has("equal child conditions",
                        new Function<AnyCondition, List<ActionCondition>>() {
                            @Override
                            public List<ActionCondition> apply(AnyCondition input) {
                                return input.getChildConditions();
                            }
                        },
                        Matchers.<List<ActionCondition>>allOf(
                                hasSize(allCondition.getChildConditions().size()),
                                everyItem(isA(ActionCondition.class)),
                                everyItem(not(isIn(allCondition.getChildConditions()))))));
    }
}
