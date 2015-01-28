package org.asoem.greyfish.utils.collect;

public class CombinedStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        final BitString lowString = BitString.parse(bitString.substring(bitString.length() / 2, bitString.length()));
        final BitString highString = BitString.parse(bitString.substring(0, bitString.length() / 2));
        return new BitString.CombinedString(lowString, highString);
    }
}
