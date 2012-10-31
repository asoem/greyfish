package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationResult;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * User: christoph
 * Date: 17.02.12
 * Time: 17:01
 */
public class ConvertingEvaluationResultTest {
    @Test
    public void testDoubleAsDouble() throws Exception {
        // given
        EvaluationResult result = new ConvertingEvaluationResult(42.0);

        // when
        final double b = result.asDouble();

        // then
        assertThat(b, is(42.0));
    }

    @Test
    public void testBooleanAsBoolean() throws Exception {
        // given
        EvaluationResult result = new ConvertingEvaluationResult(true);

        // when
        final boolean b = result.asBoolean();

        // then
        assertThat(b, is(true));
    }

    @Test
    public void testStringAsDouble() throws Exception {
        // given
        EvaluationResult result = new ConvertingEvaluationResult("42.0");

        // when
        final double b = result.asDouble();

        // then
        assertThat(b, is(42.0));
    }

    @Test
    public void testStringAsBoolean() throws Exception {
        // given
        EvaluationResult result = new ConvertingEvaluationResult("true");

        // when
        final boolean b = result.asBoolean();

        // then
        assertThat(b, is(true));
    }
}
