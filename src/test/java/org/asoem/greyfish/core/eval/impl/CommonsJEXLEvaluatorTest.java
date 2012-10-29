package org.asoem.greyfish.core.eval.impl;

import com.google.inject.Guice;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.inject.CoreModule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 15:52
 */
public class CommonsJEXLEvaluatorTest {

    public CommonsJEXLEvaluatorTest() {
        Guice.createInjector(new CoreModule());
    }

    @Test
    public void testDollarFunction() throws Exception {
        Evaluator evaluator = new CommonsJEXLEvaluator();

        evaluator.setExpression("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate(null).asDouble();

        // then
        assertThat(ret).isEqualTo(42.0);
    }
}
