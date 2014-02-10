package org.asoem.greyfish.utils.collect;

public class BitStringViewTest extends AbstractBitSequenceImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        return new BitString.BitStringView(
                new BitString.RegularBitString("010" + bitString + "101"), 3, 3 + bitString.length());
    }
}
