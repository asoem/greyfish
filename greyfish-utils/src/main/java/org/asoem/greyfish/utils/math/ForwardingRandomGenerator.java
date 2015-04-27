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

import org.apache.commons.math3.random.RandomGenerator;

/**
 * A decorator for an RandomGenerator.
 */
public abstract class ForwardingRandomGenerator implements RandomGenerator {
    protected abstract RandomGenerator delegate();

    @Override
    public void setSeed(final int seed) {
        delegate().setSeed(seed);
    }

    @Override
    public void setSeed(final int[] seed) {
        delegate().setSeed(seed);
    }

    @Override
    public void setSeed(final long seed) {
        delegate().setSeed(seed);
    }

    @Override
    public void nextBytes(final byte[] bytes) {
        delegate().nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return delegate().nextInt();
    }

    @Override
    public int nextInt(final int n) {
        return delegate().nextInt(n);
    }

    @Override
    public long nextLong() {
        return delegate().nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return delegate().nextBoolean();
    }

    @Override
    public float nextFloat() {
        return delegate().nextFloat();
    }

    @Override
    public double nextDouble() {
        return delegate().nextDouble();
    }

    @Override
    public double nextGaussian() {
        return delegate().nextGaussian();
    }
}
