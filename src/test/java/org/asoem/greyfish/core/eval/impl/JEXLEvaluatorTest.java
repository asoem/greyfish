package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

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
        Evaluator evaluator = new JEXLEvaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate().asDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }

}
