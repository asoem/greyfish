package org.asoem.greyfish.core.eval;

import javolution.io.Struct;
import org.asoem.greyfish.core.individual.GFComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
    @Mock GreyfishVariableResolver<GFComponent> greyfishVariableResolver;
    @Mock Evaluator evaluator;
    @Mock GFComponent context;

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullEvaluatorFactory() {
        new GreyfishExpression<GFComponent>("", null, greyfishVariableResolver);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullGreyfishVariableResolver() {
        new GreyfishExpression<GFComponent>("", evaluatorFactory, null);
    }

    @Test
    public void testEvaluateAsDouble() throws Exception {
        // given
        given(evaluatorFactory.createEvaluator("", greyfishVariableResolver)).willReturn(evaluator);
        given(evaluator.evaluateAsDouble()).will(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocationOnMock) throws Throwable {
                return 5.0;
            }
        });

        // when
        GreyfishExpression<GFComponent> expression = new GreyfishExpression<GFComponent>("", evaluatorFactory, greyfishVariableResolver);
        double ret = expression.evaluateAsDouble(context);

        // then
        assertEquals(ret, 5.0, 0);
    }

    @Test
    public void testEvaluateAsBoolean() throws Exception {
        // given
        given(evaluatorFactory.createEvaluator("", greyfishVariableResolver)).willReturn(evaluator);
        given(evaluator.evaluateAsBoolean()).will(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                return true;
            }
        });

        // when
        GreyfishExpression<GFComponent> expression = new GreyfishExpression<GFComponent>("", evaluatorFactory, greyfishVariableResolver);
        boolean ret = expression.evaluateAsBoolean(context);

        // then
        assert(ret);
    }
}
