package org.asoem.greyfish.core.eval;

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
        evaluator.setExpression("3*2");

        // when
        double ret = evaluator.evaluateAsDouble();

        // then
        assertThat(ret).isEqualTo(6);
    }
}
