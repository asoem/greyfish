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
