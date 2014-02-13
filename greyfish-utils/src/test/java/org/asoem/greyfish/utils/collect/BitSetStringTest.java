package org.asoem.greyfish.utils.collect;

public class BitSetStringTest extends AbstractBitStringImplementationTest {

    @Override
    protected BitString createSequence(final String bitString) {
        return new BitString.BitSetString(BitSets.parse(bitString), bitString.length());
    }

}
