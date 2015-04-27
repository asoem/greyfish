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

package org.asoem.greyfish.utils.math;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class PeriodicFunctionsTest {
    @Test
    public void testTriangleWave() throws Exception {
        final UnivariateFunction wave = PeriodicFunctions.triangleWave(1.0, 0.0);

        final List<Double> generated = Lists.newArrayList();
        BigDecimal x = BigDecimal.valueOf(-1.0);
        while (x.doubleValue() <= 1.0) {
            final double y = wave.value(x.doubleValue());
            generated.add(y);
            x = x.add(BigDecimal.valueOf(0.1));
        }

        System.out.print(Joiner.on("\n").join(generated));

        final ImmutableList<Double> expected = ImmutableList.of(0.0, 0.4, 0.8, 0.8, 0.4, 0.0, -0.4, -0.8, -0.8, -0.4, 0.0, 0.4, 0.8, 0.8, 0.4, 0.0, -0.4, -0.8, -0.8, -0.4, 0.0);
        assertThat(generated, hasSize(expected.size()));
        for (int i = 0; i < generated.size(); i++) {
            assertThat(generated.get(i), is(closeTo(expected.get(i), 0.000000000000001)));
        }
    }

    @Test
    public void testSawtoothWave() throws Exception {
        final UnivariateFunction wave = PeriodicFunctions.sawtoothWave(1.0, 0.0);

        final List<Double> generated = Lists.newArrayList();
        BigDecimal x = BigDecimal.valueOf(-1.0);
        while (x.doubleValue() <= 1.0) {
            final double y = wave.value(x.doubleValue());
            generated.add(y);
            x = x.add(BigDecimal.valueOf(0.1));
        }

        System.out.print(Joiner.on("\n").join(generated));

        final ImmutableList<Double> expected = ImmutableList.of(0.0, 0.2, 0.4, 0.6, 0.8, -1.0, -0.8, -0.6, -0.4, -0.2, 0.0, 0.2, 0.4, 0.6, 0.8, -1.0, -0.8, -0.6, -0.4, -0.2, 0.0);
        assertThat(generated, hasSize(expected.size()));
        for (int i = 0; i < generated.size(); i++) {
            assertThat(generated.get(i), is(closeTo(expected.get(i), 0.000000000000001)));
        }
    }

    @Test
    public void testSquareWave() throws Exception {
        final UnivariateFunction wave = PeriodicFunctions.squareWave(1.0, 0.0);

        final List<Double> generated = Lists.newArrayList();
        BigDecimal x = BigDecimal.valueOf(-1.0);
        while (x.doubleValue() <= 1.0) {
            final double y = wave.value(x.doubleValue());
            generated.add(y);
            x = x.add(BigDecimal.valueOf(0.1));
        }

        System.out.print(Joiner.on("\n").join(generated));

        final ImmutableList<Double> expected = ImmutableList.of(1.0, 1.0, 1.0, 1.0, 1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 1.0, 1.0, 1.0, 1.0, 1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 1.0);
        assertThat(generated, hasSize(expected.size()));
        for (int i = 0; i < generated.size(); i++) {
            assertThat(generated.get(i), is(closeTo(expected.get(i), 0.000000000000001)));
        }
    }
}
