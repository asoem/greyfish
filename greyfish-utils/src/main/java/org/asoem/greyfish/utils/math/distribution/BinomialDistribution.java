package org.asoem.greyfish.utils.math.distribution;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.special.Beta;

public final class BinomialDistribution extends AbstractIntegerDistribution {
    /**
     * Serializable version identifier.
     */
    private static final long serialVersionUID = 6751309484392813623L;
    /**
     * The number of trials.
     */
    private final int numberOfTrials;
    /**
     * The probability of success.
     */
    private final double probabilityOfSuccess;

    /**
     * Create a binomial distribution with the given number of trials and probability of success.
     *
     * @param trials Number of trials.
     * @param p      Probability of success.
     * @throws NotPositiveException if {@code trials < 0}.
     * @throws OutOfRangeException  if {@code p < 0} or {@code p > 1}.
     */
    public BinomialDistribution(final int trials, final double p) {
        this(new Well19937c(), trials, p);
    }

    /**
     * Creates a binomial distribution.
     *
     * @param rng    Random number generator.
     * @param trials Number of trials.
     * @param p      Probability of success.
     * @throws NotPositiveException if {@code trials < 0}.
     * @throws OutOfRangeException  if {@code p < 0} or {@code p > 1}.
     * @since 3.1
     */
    public BinomialDistribution(final RandomGenerator rng,
                                final int trials,
                                final double p) {
        super(rng);

        if (trials < 0) {
            throw new NotPositiveException(LocalizedFormats.NUMBER_OF_TRIALS,
                    trials);
        }
        if (p < 0 || p > 1) {
            throw new OutOfRangeException(p, 0, 1);
        }

        probabilityOfSuccess = p;
        numberOfTrials = trials;
    }

    /**
     * Access the number of trials for this distribution.
     *
     * @return the number of trials.
     */
    public int getNumberOfTrials() {
        return numberOfTrials;
    }

    /**
     * Access the probability of success for this distribution.
     *
     * @return the probability of success.
     */
    public double getProbabilityOfSuccess() {
        return probabilityOfSuccess;
    }

    /**
     * {@inheritDoc}
     */
    public double probability(final int x) {
        return new org.apache.commons.math3.distribution.BinomialDistribution(numberOfTrials, probabilityOfSuccess).probability(x);
    }

    /**
     * {@inheritDoc}
     */
    public double cumulativeProbability(final int x) {
        final double ret;
        if (x < 0) {
            ret = 0.0;
        } else if (x >= numberOfTrials) {
            ret = 1.0;
        } else {
            ret = 1.0 - Beta.regularizedBeta(probabilityOfSuccess,
                    x + 1.0, numberOfTrials - x);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For {@code n} trials and probability parameter {@code p}, the mean is {@code n * p}.
     */
    public double getNumericalMean() {
        return numberOfTrials * probabilityOfSuccess;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For {@code n} trials and probability parameter {@code p}, the variance is {@code n * p * (1 - p)}.
     */
    public double getNumericalVariance() {
        final double p = probabilityOfSuccess;
        return numberOfTrials * p * (1 - p);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The lower bound of the support is always 0 except for the probability parameter {@code p = 1}.
     *
     * @return lower bound of the support (0 or the number of trials)
     */
    public int getSupportLowerBound() {
        return probabilityOfSuccess < 1.0 ? 0 : numberOfTrials;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The upper bound of the support is the number of trials except for the probability parameter {@code p = 0}.
     *
     * @return upper bound of the support (number of trials or 0)
     */
    public int getSupportUpperBound() {
        return probabilityOfSuccess > 0.0 ? numberOfTrials : 0;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    public boolean isSupportConnected() {
        return true;
    }
}
