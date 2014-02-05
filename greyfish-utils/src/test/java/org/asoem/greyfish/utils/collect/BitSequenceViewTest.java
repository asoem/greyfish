package org.asoem.greyfish.utils.collect;

public class BitSequenceViewTest extends AbstractBitSequenceImplementationTest {
    @Override
    protected BitSequence createSequence(final String bitString) {
        return new BitSequence.BitSequenceView(
                new BitSequence.RegularBitSequence("010" + bitString + "101"), 3, 3 + bitString.length());
    }
}
