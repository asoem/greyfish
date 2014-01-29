package org.asoem.greyfish.utils.collect;

public class RegularBitSequenceTest extends BitSequenceTest {

    @Override
    protected BitSequence createSequence(final String bitString) {
        return new BitSequence.RegularBitSequence(bitString);
    }

}
