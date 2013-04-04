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

/**
 * User: christoph
 * Date: 21.01.13
 * Time: 14:25
 */
public class PeriodicFunctionsTest {
    @Test
    public void testTriangleWave() throws Exception {
        UnivariateFunction wave = PeriodicFunctions.triangleWave(1.0, 0.0);
        
        List<Double> generated = Lists.newArrayList();
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
        UnivariateFunction wave = PeriodicFunctions.sawtoothWave(1.0, 0.0);

        List<Double> generated = Lists.newArrayList();
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
        UnivariateFunction wave = PeriodicFunctions.squareWave(1.0, 0.0);

        List<Double> generated = Lists.newArrayList();
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
