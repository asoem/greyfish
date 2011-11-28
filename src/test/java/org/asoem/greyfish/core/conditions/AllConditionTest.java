package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    @Mock GFCondition condition;

    @Test
    public void testAll() throws Exception {
        // when
        AllCondition allCondition = AllCondition.all(condition, condition);

        // then
        assertThat(allCondition).hasSize(2);
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        given(condition.deepClone(any(DeepCloner.class))).will(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                DeepCloner.class.cast(args[0]).setAsCloned(condition, condition);
                return condition;
            }
        });
        AllCondition allCondition = AllCondition.all(condition, condition);

        // when
        AllCondition clone = DeepCloner.clone(allCondition, AllCondition.class);

        // then
        assertThat(clone).containsOnly(condition, condition);
    }
}
