/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
