package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 13.02.12
 * Time: 16:46
 */
public class JRubyEvaluatorTest {

    public JRubyEvaluatorTest() {
        CoreInjectorHolder.coreInjector();
    }

    @Test
    public void testDollarFunction() throws Exception {
        Evaluator evaluator = new JRubyEvaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluateAsDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }
}
