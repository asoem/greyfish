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

package org.asoem.greyfish.utils.math.statistics;

import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.stat.StatUtils;

final class DefaultFTest implements FTest {
    private final int numeratorDegreesOfFreedom;
    private final int denominatorDegreesOfFreedom;
    private final double ratio;
    private final double p;

    DefaultFTest(final double[] sample1, final double[] sample2) {
        final double variance1 = StatUtils.variance(sample1);
        final double variance2 = StatUtils.variance(sample2);

        this.numeratorDegreesOfFreedom = sample1.length - 1;
        this.denominatorDegreesOfFreedom = sample2.length - 1;
        final FDistribution fDistribution = new FDistribution(numeratorDegreesOfFreedom, denominatorDegreesOfFreedom);
        this.ratio = variance2 / variance1;
        this.p = 2 * (1 - fDistribution.probability(ratio));
    }

    @Override
    public double p() {
        return p;
    }

    @Override
    public double ratio() {
        return ratio;
    }

    @Override
    public int numeratorDegreesOfFreedom() {
        return numeratorDegreesOfFreedom;
    }

    @Override
    public int denominatorDegreesOfFreedom() {
        return denominatorDegreesOfFreedom;
    }
}
