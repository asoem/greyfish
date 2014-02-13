package org.asoem.greyfish.utils.collect;

import com.google.common.collect.Sets;

import java.util.HashSet;

public class IndexSetStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        final HashSet<Integer> indices = Sets.newHashSet();
        for (int i = bitString.length() - 1, j = 0; i >= 0; i--, j++) {

            if (bitString.charAt(i) == '1') {
                indices.add(j);
            }
        }
        return new BitString.IndexSetString(indices, bitString.length());
    }
}
