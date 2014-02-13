package org.asoem.greyfish.utils.collect;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class InverseStringTest extends AbstractBitStringImplementationTest {
    @Override
    protected BitString createSequence(final String bitString) {
        return new BitString.InverseString(BitString.create(
                Iterables.transform(Lists.charactersOf(new StringBuffer(bitString).reverse().toString()),
                        new Function<Character, Boolean>() {
                            @Override
                            public Boolean apply(final Character input) {
                                return input.equals('0');
                            }
                        })));
    }
}
