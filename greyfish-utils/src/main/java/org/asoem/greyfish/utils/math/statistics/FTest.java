package org.asoem.greyfish.utils.math.statistics;

public interface FTest {
    double p();

    double ratio();

    int numeratorDegreesOfFreedom();

    int denominatorDegreesOfFreedom();
}
