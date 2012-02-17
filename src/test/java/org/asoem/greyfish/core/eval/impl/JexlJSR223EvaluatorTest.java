package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 11:24
 */
public class JEXLJSR223EvaluatorTest {
    public JEXLJSR223EvaluatorTest() {
        CoreInjectorHolder.coreInjector();
    }

    @Test
    public void testDollarFunction() throws Exception {
        Evaluator evaluator = new JEXLJSR223Evaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate().asDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }
}
