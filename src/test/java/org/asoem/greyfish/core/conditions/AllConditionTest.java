package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    @Mock
    ActionCondition condition;

    @Test
    public void testAll() throws Exception {
        // when
        AllCondition allCondition = AllCondition.evaluates(condition, condition);

        // then
        assertThat(allCondition).hasSize(2);
    }

    @Test
    public void testDeepClone() throws Exception {
        // given
        ActionCondition condition = mock(ActionCondition.class);
        ActionCondition conditionClone = mock(ActionCondition.class);
        given(condition.deepClone(any(DeepCloner.class))).willReturn(conditionClone);
        AllCondition allCondition = AllCondition.evaluates(condition, condition);

        // when
        AllCondition clone = DeepCloner.clone(allCondition, AllCondition.class);

        // then
        verify(condition, times(2)).deepClone(any(DeepCloner.class));
        assertThat(clone).containsOnly(conditionClone, conditionClone);
    }
}
