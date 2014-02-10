package org.asoem.greyfish.utils.collect;

public class CombinedSequenceTest extends AbstractBitSequenceImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        return new BitString.CombinedString(
                BitString.parse(bitString.substring(bitString.length() / 2, bitString.length())),
                BitString.parse(bitString.substring(0, bitString.length() / 2)));
    }
}
