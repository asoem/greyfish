package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
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
        final DoubleProperty<DefaultGreyfishAgent> doubleProperty = DoubleProperty.<DefaultGreyfishAgent>with()
                .name("test")
                .lowerBound(3.0)
                .upperBound(7.0)
                .initialValue(4.0)
                .build();
        
        // when
        final DoubleProperty<DefaultGreyfishAgent> persistent = Persisters.createCopy(doubleProperty, Persisters.javaSerialization());
        
        // then
        MatcherAssert.assertThat(persistent, is(equalTo(doubleProperty)));
    }
}
