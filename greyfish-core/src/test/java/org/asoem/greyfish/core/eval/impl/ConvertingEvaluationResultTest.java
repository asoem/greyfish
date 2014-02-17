package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationResult;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class ConvertingEvaluationResultTest {
    @Test
    public void testDoubleAsDouble() throws Exception {
        // given
        final EvaluationResult result = new ConvertingEvaluationResult(42.0);

        // when
        final double b = result.asDouble();

        // then
        assertThat(b, is(42.0));
    }

    @Test
    public void testBooleanAsBoolean() throws Exception {
        // given
        final EvaluationResult result = new ConvertingEvaluationResult(true);

        // when
        final boolean b = result.asBoolean();

        // then
        assertThat(b, is(true));
    }

    @Test
    public void testStringAsDouble() throws Exception {
        // given
        final EvaluationResult result = new ConvertingEvaluationResult("42.0");

        // when
        final double b = result.asDouble();

        // then
        assertThat(b, is(42.0));
    }

    @Test
    public void testStringAsBoolean() throws Exception {
        // given
        final EvaluationResult result = new ConvertingEvaluationResult("true");

        // when
        final boolean b = result.asBoolean();

        // then
        assertThat(b, is(true));
    }
}
