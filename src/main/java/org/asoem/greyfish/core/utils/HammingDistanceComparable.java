package org.asoem.greyfish.core.utils;

/**
 * User: christoph
 * Date: 04.04.11
 * Time: 15:38
 */
public interface HammingDistanceComparable {
    Object[] getHammingString();
    int hammingDistance(HammingDistanceComparable b);
}
