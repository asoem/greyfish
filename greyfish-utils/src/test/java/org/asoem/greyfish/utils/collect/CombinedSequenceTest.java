package org.asoem.greyfish.utils.collect;

public class CombinedSequenceTest extends AbstractBitSequenceImplementationTest {
    @Override
    protected BitSequence createSequence(final String bitString) {
        return new BitSequence.CombinedSequence(
                BitSequence.parse(bitString.substring(bitString.length() / 2, bitString.length())),
                BitSequence.parse(bitString.substring(0, bitString.length() / 2)));
    }
}
