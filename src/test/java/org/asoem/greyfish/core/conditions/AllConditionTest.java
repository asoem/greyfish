package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.utils.base.DeepCloner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AllConditionTest {

    @Test
    public void testDeepClone() throws Exception {
        // given
        final DeepCloner clonerMock = mock(DeepCloner.class);
        final ActionCondition mock = mock(ActionCondition.class);
        given(clonerMock.getClone(any(ActionCondition.class), eq(ActionCondition.class))).willReturn(mock);
        final AllCondition allCondition = AllCondition.evaluates(mock, mock);

        // when
        AllCondition clone = allCondition.deepClone(clonerMock);

        // then
        assertThat(clone).containsOnly(mock, mock);
    }
}
