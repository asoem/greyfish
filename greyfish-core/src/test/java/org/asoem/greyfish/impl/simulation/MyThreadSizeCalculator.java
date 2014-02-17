package org.asoem.greyfish.impl.simulation;

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