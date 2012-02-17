package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.impl.SeeEvaluator;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
/**
 * User: christoph
 * Date: 22.09.11
 * Time: 12:10
 */
public class SeeEvaluatorTest {

    @Test
    public void testEvaluateAsDouble() throws EvaluationException {
        // given
        SeeEvaluator evaluator = new SeeEvaluator();
        evaluator.setExpression("3.0 * 2");

        // when
        double ret = evaluator.evaluate().asDouble();

        // then
        assertThat(ret).isEqualTo(6);
    }
}
