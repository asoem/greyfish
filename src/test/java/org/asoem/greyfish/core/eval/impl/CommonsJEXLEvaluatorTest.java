package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.VariableResolvers;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: christoph
 * Date: 17.02.12
 * Time: 15:52
 */
public class CommonsJEXLEvaluatorTest {

    @Test
    public void testDollarFunction() throws Exception {
        // given
        Evaluator evaluator = new CommonsJEXLEvaluator("$('testVal', 'hello')");

        // when
        final double ret = evaluator.evaluate(VariableResolvers.emptyResolver()).asDouble();

        // then
        assertThat(ret, is(equalTo(42.0)));
    }
}
