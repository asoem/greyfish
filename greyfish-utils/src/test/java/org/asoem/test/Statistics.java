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

package org.asoem.test;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.asoem.greyfish.utils.math.statistics.StatisticalTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Statistics {

    private static final Logger logger = LoggerFactory.getLogger(Statistics.class);

    private Statistics() {
    }

    /**
     * Assert that the mean of the measurements of the {@code treatment} are significantly less high than those of the
     * {@code control}.
     *
     * @param control   the measurements of the control
     * @param treatment the measurements of the treatment
     * @param alpha     the required significance level (like {@link org.asoem.greyfish.utils.math.SignificanceLevel#getAlpha()})
     */
    public static void assertSignificantDecrease(final DescriptiveStatistics control, final DescriptiveStatistics treatment, final double alpha) {
        // Is it faster?
        logger.info("Treatment vs. Control: {}, {}",
                treatment, control);

        assertThat("The mean of the treatment " +
                "is not less than the mean of the control",
                treatment.getMean(), is(lessThan(control.getMean())));

        // Is it also significantly faster? Make a t-test.
        // Test assumptions for t-test: normality
        assertThat("The treatment is not normal distributed", StatisticalTests.shapiroWilk(treatment.getValues()).p(), is(lessThan(alpha)));
        assertThat("The control is not normal distributed", StatisticalTests.shapiroWilk(control.getValues()).p(), is(lessThan(alpha)));

        // Perform the t-test
        final double t = new TTest().t(treatment, control);
        final double p = new TTest().tTest(treatment, control);
        logger.info("t-test: t={}, p={}", t, p);
        double qt = new TDistribution(treatment.getN() - 1 + control.getN() - 1).inverseCumulativeProbability(1 - alpha / 2);
        assertThat("The means are not significantly different", Math.abs(t), is(greaterThan(qt)));
    }
}
