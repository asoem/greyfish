package org.asoem.greyfish.utils.collect;

public class SubStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        return BitString.parse("010" + bitString + "101")
                .new SubString(3, bitString.length());
    }
}
