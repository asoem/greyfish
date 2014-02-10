package org.asoem.greyfish.utils.collect;

public class RegularBitStringTest extends AbstractBitSequenceImplementationTest {

    @Override
    protected BitString createSequence(final String bitString) {
        return new BitString.RegularBitString(bitString);
    }

}
