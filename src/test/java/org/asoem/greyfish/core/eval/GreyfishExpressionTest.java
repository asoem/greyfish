package org.asoem.greyfish.core.eval;

import com.google.inject.Guice;
import org.asoem.greyfish.core.eval.impl.EvaluatorFake;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.asoem.greyfish.utils.persistence.Persisters.createCopy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
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
    public void testEvaluateAsDouble() throws Exception {
        // given
        EvaluationResult evaluationResult = mock(EvaluationResult.class);
        final Evaluator evaluator = mock(Evaluator.class);
        given(evaluator.evaluate(any(VariableResolver.class))).willReturn(evaluationResult);
        given(evaluationResult.asDouble()).willReturn(5.0);     

        // when
        GreyfishExpression expression = new GreyfishExpression("", new EvaluatorFactory() {
            @Override
            public Evaluator createEvaluator(String expression) throws SyntaxException {
                return evaluator;
            }
        });
        double ret = expression.evaluateForContext(mock(Object.class)).asDouble();

        // then
        assertThat(ret, is(equalTo(5.0)));
    }

    @Test
    public void testEvaluateAsBoolean() throws Exception {
        // given
        EvaluationResult evaluationResult = mock(EvaluationResult.class);
        final Evaluator evaluator = mock(Evaluator.class);
        given(evaluator.evaluate(any(VariableResolver.class))).willReturn(evaluationResult);
        given(evaluationResult.asBoolean()).willReturn(true);

        // when
        GreyfishExpression expression = new GreyfishExpression("", new EvaluatorFactory() {
            @Override
            public Evaluator createEvaluator(String expression) throws SyntaxException {
                return evaluator;
            }
        });
        boolean ret = expression.evaluateForContext(mock(Object.class)).asBoolean();

        // then
        assertThat(ret, is(true));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final GreyfishExpression expression = new GreyfishExpression("42.0", new EvaluatorFactory() {
            @Override
            public Evaluator createEvaluator(String expression) throws SyntaxException {
                return EvaluatorFake.INSTANCE;
            }
        });

        // when
        final GreyfishExpression copy = createCopy(expression, JavaPersister.INSTANCE);

        // then
        assertThat(copy, both(is(equalTo(expression))).and(is(not(sameInstance((Object) expression)))));
    }
}
