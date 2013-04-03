package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.base.CycleCloner;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    @Test
    public void testDeepClone() throws Exception {
        // given
        final ActionCondition<DefaultGreyfishAgent> childMock = mock(ActionCondition.class);
        when(childMock.deepClone(any(DeepCloner.class))).thenReturn(mock(ActionCondition.class));
        final AllCondition<DefaultGreyfishAgent> allCondition = AllCondition.evaluates(childMock, childMock);

        // when
        AllCondition<DefaultGreyfishAgent> clone = CycleCloner.clone(allCondition);

        // then
        assertThat(clone.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
        verify(childMock, times(2)).deepClone(any(DeepCloner.class));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<DefaultGreyfishAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        AllCondition<DefaultGreyfishAgent> allCondition = AllCondition.evaluates(condition, condition);

        // when
        final AllCondition<DefaultGreyfishAgent> copy = Persisters.createCopy(allCondition, Persisters.javaSerialization());

        // then
        assertThat(copy.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
    }
}
