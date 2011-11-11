package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 16:13
 */
@RunWith(MockitoJUnitRunner.class)
public class GreyfishExpressionTest {

    @Mock EvaluatorFactory evaluatorFactory;
    @Mock GreyfishVariableResolver<AgentComponent> greyfishVariableResolver;
    @Mock Evaluator evaluator;
    @Mock AgentComponent context;

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullEvaluatorFactory() {
        new GreyfishExpression("", null);
    }

    @Test
    public void testEvaluateAsDouble() throws Exception {
        // given
        given(evaluatorFactory.createEvaluator("")).willReturn(evaluator);
        given(evaluator.evaluateAsDouble()).will(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocationOnMock) throws Throwable {
                return 5.0;
            }
        });

        // when
        GreyfishExpression expression = new GreyfishExpression("", evaluator);
        double ret = expression.evaluateAsDouble(context);

        // then
        assertEquals(ret, 5.0, 0);
    }

    @Test
    public void testEvaluateAsBoolean() throws Exception {
        // given
        given(evaluatorFactory.createEvaluator("")).willReturn(evaluator);
        given(evaluator.evaluateAsBoolean()).will(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return true;
            }
        });

        // when
        GreyfishExpression expression = new GreyfishExpression("", evaluator);
        boolean ret = expression.evaluateAsBoolean(context);

        // then
        assert(ret);
    }

    @Test
    public void testEquals() throws Exception {
        // given
        String expression = "1+2";

        // when
        GreyfishExpression expression1 = SingletonGreyfishExpressionFactory.compileExpression(expression);
        GreyfishExpression expression2 = SingletonGreyfishExpressionFactory.compileExpression(expression);

        // than
        assertThat(expression1).isEqualTo(expression2);
    }
}
