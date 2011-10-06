package org.asoem.greyfish.core.eval;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 16:46
 */

@RunWith(MockitoJUnitRunner.class)
public class MvelEvaluatorTest {

    @Mock
    VariableResolver resolver;

    @Test
    public void testSetResolver() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("a + 3.0");
        evaluator.setResolver(resolver);
        given(resolver.resolve("a")).willReturn(3.0, 4.0);

        // when
        double ret1 = evaluator.evaluateAsDouble();
        double ret2 = evaluator.evaluateAsDouble();

        // then
        verify(resolver, times(2)).resolve("a");
        assertEquals(6.0, ret1, 0);
        assertEquals(7.0, ret2, 0);
    }

    @Test
    public void testCustomParserContext_max() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("max(4.0, 5.0)");

        // when
        double ret = evaluator.evaluateAsDouble();

        // then
        assertEquals(5.0, ret, 0);
    }

    @Test
    public void testCustomParserContext_gaussian() throws EvaluationException {
        // given
        MvelEvaluator evaluator = new MvelEvaluator();
        evaluator.setExpression("gaussian(0.0, 0.2)");

        // when
        double ret = 0.0;
        for (int i = 0; i < 1000; i++) {
              ret += evaluator.evaluateAsDouble();
        }
        ret = ret / 1000;

        // then
        assertEquals(0.0, ret, 0.01);
    }
}