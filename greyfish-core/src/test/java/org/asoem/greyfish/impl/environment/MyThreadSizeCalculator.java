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

package org.asoem.greyfish.impl.environment;

import com.google.common.base.Supplier;
import org.apache.commons.math3.random.RandomGenerator;
import org.asoem.greyfish.utils.math.ApproximationMath;
import org.asoem.greyfish.utils.math.RandomGenerators;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyThreadSizeCalculator extends PoolSizeCalculator {

    private static final RandomGenerator RANDOM_GENERATOR = RandomGenerators.threadLocalGenerator(new Supplier<RandomGenerator>() {
        @Override
        public RandomGenerator get() {
            return RandomGenerators.rng();
        }
    });

    public static void main(String[] args) {
        MyThreadSizeCalculator calculator = new MyThreadSizeCalculator();
        calculator.calculateBoundaries(new BigDecimal(1.0),
                new BigDecimal(100000));
    }

    @Override
    protected long getCurrentThreadCPUTime() {
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

    @Override
    protected Runnable createTask() {
        return new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    ApproximationMath.gaussian(RANDOM_GENERATOR.nextDouble(), RANDOM_GENERATOR.nextDouble(), RANDOM_GENERATOR.nextDouble(), RANDOM_GENERATOR.nextDouble());
                }
            }
        };
    }

    @Override
    protected BlockingQueue<Runnable> createWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

}