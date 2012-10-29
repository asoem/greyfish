package org.asoem.greyfish.core.eval;

import com.google.inject.Guice;
import org.asoem.greyfish.core.eval.impl.EvaluatorFake;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
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
        Guice.createInjector(new CoreModule()).injectMembers(this);
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
        given(evaluator.evaluate(null)).willReturn(evaluationResult);
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
        given(evaluator.evaluate(null)).willReturn(evaluationResult);
        given(evaluationResult.asBoolean()).willReturn(true);

        // when
        GreyfishExpression expression = new GreyfishExpression("", evaluator);
        boolean ret = expression.evaluateForContext(mock(Object.class)).asBoolean();

        // then
        assertThat(ret).isTrue();
    }

    @Test
    public void testGreyfishExpression() throws Exception {
        // given
        final GreyfishExpression expression = new GreyfishExpression("42.0", EvaluatorFake.INSTANCE);

        // when
        final GreyfishExpression deserialized = Persisters.createCopy(expression, JavaPersister.INSTANCE);

        // then
        assertThat(deserialized).isEqualTo(expression);
    }
}
