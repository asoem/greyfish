package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.impl.JavaScriptEvaluator;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 10.02.12
 * Time: 17:49
 */
public class JavaScriptEvaluatorTest {

    public JavaScriptEvaluatorTest() {
        CoreInjectorHolder.coreInjector();
    }

    @Test
    public void testDollarFunction() throws Exception {
        Evaluator evaluator = new JavaScriptEvaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate().asDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }
}
