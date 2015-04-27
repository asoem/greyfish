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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for statistical tests.
 */
public final class StatisticalTests {
    private StatisticalTests() {
    }

    /**
     * Performs an F test to compare the variances of two samples from normal populations. <p>Wikipedia: <a
     * href="https://en.wikipedia.org/wiki/F-test">https://en.wikipedia.org/wiki/F-test</a></p>
     *
     * @param sample1 the first sample
     * @param sample2 the second sample
     * @return a test summary
     */
    public static FTest f(final double[] sample1, final double[] sample2) {
        checkNotNull(sample1);
        checkNotNull(sample2);
        return new DefaultFTest(sample1, sample2);
    }

    /**
     * Performs the Shapiro-Wilk test of normality. <p>This algorithm is limited to samples of size in the range [3,
     * 5000]</p> <p>Wikipedia: <a href="https://en.wikipedia.org/wiki/Shapiro_wilk">https://en.wikipedia.org/wiki/Shapiro_wilk</a>
     * </p>
     *
     * @param sample the sample to test
     * @return the test summary
     */
    public static ShapiroWilkTest shapiroWilk(final double[] sample) {
        checkNotNull(sample);
        checkArgument(sample.length >= 3 && sample.length <= 5000,
                "Sample size must be in range [3, 5000], was %s", sample.length);
        return new DefaultShapiroWilkTest(sample);
    }
}
