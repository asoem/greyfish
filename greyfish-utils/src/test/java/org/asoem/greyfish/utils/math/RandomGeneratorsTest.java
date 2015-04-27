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

import com.google.common.base.Supplier;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class RandomGeneratorsTest {

    @Test
    public void testThreadLocalGeneratorOneThread() {
        // given
        final Supplier generatorSupplier = mock(Supplier.class);
        final RandomGenerator randomGeneratorThread1 = mock(RandomGenerator.class);
        final RandomGenerator randomGeneratorThread2 = mock(RandomGenerator.class);
        given(generatorSupplier.get()).willReturn(randomGeneratorThread1, randomGeneratorThread2);
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(generatorSupplier);

        // when
        threadLocalRandomGenerator.nextBoolean();
        threadLocalRandomGenerator.nextBoolean();

        // then
        verify(randomGeneratorThread1, times(2)).nextBoolean();
        verify(randomGeneratorThread2, never()).nextBoolean();
    }

    @Test
    public void testThreadLocalGeneratorTwoThreads() throws ExecutionException, InterruptedException {
        // given
        final Supplier generatorSupplier = mock(Supplier.class);
        final RandomGenerator randomGeneratorThread1 = mock(RandomGenerator.class);
        final RandomGenerator randomGeneratorThread2 = mock(RandomGenerator.class);
        given(generatorSupplier.get()).willReturn(randomGeneratorThread1, randomGeneratorThread2);
        final RandomGenerator threadLocalRandomGenerator = RandomGenerators.threadLocalGenerator(generatorSupplier);

        // when
        Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return threadLocalRandomGenerator.nextBoolean();
            }
        }).get();
        threadLocalRandomGenerator.nextBoolean();

        // then
        verify(randomGeneratorThread1).nextBoolean();
        verify(randomGeneratorThread2).nextBoolean();
    }
}
