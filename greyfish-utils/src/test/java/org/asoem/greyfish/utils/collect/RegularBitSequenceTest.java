package org.asoem.greyfish.utils.collect;

public class RegularBitSequenceTest extends AbstractBitSequenceImplementationTest {

    @Override
    protected BitSequence createSequence(final String bitString) {
        return new BitSequence.RegularBitSequence(bitString);
    }

}
