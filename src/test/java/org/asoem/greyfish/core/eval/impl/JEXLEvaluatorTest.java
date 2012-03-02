package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.VariableResolver;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 15:52
 */
public class JEXLEvaluatorTest {

    public JEXLEvaluatorTest() {
        CoreInjectorHolder.coreInjector();
    }

    @Test
    public void testDollarFunction() throws Exception {
        Evaluator evaluator = new JexlEvaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate().asDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }

    @Test
    public void testEquals() throws Exception {
        // given
        final VariableResolver resolver = mock(VariableResolver.class);
        final String expression = "42.0";
        final JexlEvaluator evaluator1 = new JexlEvaluator();
        evaluator1.setExpression(expression);
        evaluator1.setResolver(resolver);
        final JexlEvaluator evaluator2 = new JexlEvaluator();
        evaluator2.setExpression(expression);
        evaluator2.setResolver(resolver);

        // when
        final boolean bothAreEqual = evaluator1.equals(evaluator2);

        // then
        assertThat(bothAreEqual).isTrue();
    }
}
