package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    /*
    @Test
    public void testDeepClone() throws Exception {
        // given
        final ActionCondition<Basic2DAgent> childMock = mock(ActionCondition.class);
        when(childMock.deepClone(any(DeepCloner.class))).thenReturn(mock(ActionCondition.class));
        final AllCondition<Basic2DAgent> allCondition = AllCondition.evaluates(childMock, childMock);

        // when
        final AllCondition<Basic2DAgent> clone = CycleCloner.clone(allCondition);

        // then
        assertThat(clone.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
        verify(childMock, times(2)).deepClone(any(DeepCloner.class));
    }
    */
    @Test
    public void testSerialization() throws Exception {
        // given
        final ActionCondition<Basic2DAgent> condition = mock(ActionCondition.class, withSettings().serializable());
        final AllCondition<Basic2DAgent> allCondition = AllCondition.evaluates(condition, condition);

        // when
        final AllCondition<Basic2DAgent> copy = Persisters.copyAsync(allCondition, Persisters.javaSerialization());

        // then
        assertThat(copy.getChildConditions(), hasSize(allCondition.getChildConditions().size()));
    }
}
