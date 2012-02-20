package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 16:13
 */
@RunWith(MockitoJUnitRunner.class)
public class GreyfishExpressionTest {

    public GreyfishExpressionTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationWithNullEvaluatorFactory() {
        new GreyfishExpression("", null);
    }

    @Test
    public void testGetExpression() throws Exception {
        // given
        String foobar = "1+2";
        GreyfishExpression expression = new GreyfishExpression(foobar, mock(Evaluator.class));

        // when
        String expressionStr = expression.getExpression();

        // then
        assertThat(expressionStr).isEqualTo(foobar);
    }

    @Test
    public void testEvaluateAsDouble() throws Exception {
        // given
        EvaluationResult evaluationResult = mock(EvaluationResult.class);
        Evaluator evaluator = mock(Evaluator.class);
        given(evaluator.evaluate()).willReturn(evaluationResult);
        given(evaluationResult.asDouble()).willReturn(5.0);     

        // when
        GreyfishExpression expression = new GreyfishExpression("", evaluator);
        double ret = expression.evaluateForContext(mock(Object.class)).asDouble();

        // then
        assertThat(ret).isEqualTo(5.0);
    }

    @Test
    public void testEvaluateAsBoolean() throws Exception {
        // given
        EvaluationResult evaluationResult = mock(EvaluationResult.class);
        Evaluator evaluator = mock(Evaluator.class);
        given(evaluator.evaluate()).willReturn(evaluationResult);
        given(evaluationResult.asBoolean()).willReturn(true);

        // when
        GreyfishExpression expression = new GreyfishExpression("", evaluator);
        boolean ret = expression.evaluateForContext(mock(Object.class)).asBoolean();

        // then
        assertThat(ret).isTrue();
    }
}
