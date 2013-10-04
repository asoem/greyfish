package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:09
 */
public class DoublePropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final DoubleProperty<Basic2DAgent> doubleProperty = DoubleProperty.<Basic2DAgent>with()
                .name("test")
                .lowerBound(3.0)
                .upperBound(7.0)
                .initialValue(4.0)
                .build();
        
        // when
        final DoubleProperty<Basic2DAgent> persistent = Persisters.copyAsync(doubleProperty, Persisters.javaSerialization());
        
        // then
        MatcherAssert.assertThat(persistent, is(equalTo(doubleProperty)));
    }
}
