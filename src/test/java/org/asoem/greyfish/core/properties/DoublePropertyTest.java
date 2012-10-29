package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:09
 */
public class DoublePropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final DoubleProperty doubleProperty = DoubleProperty.with().name("test").lowerBound(3.0).upperBound(7.0).initialValue(4.0).build();
        
        // when
        final DoubleProperty persistent = Persisters.createCopy(doubleProperty, JavaPersister.INSTANCE);
        
        // then
        assertThat(persistent.getName()).isEqualTo("test");
        assertThat(persistent.getInitialValue()).isEqualTo(4.0);
        assertThat(persistent.getRange().lowerEndpoint()).isEqualTo(3.0);
        assertThat(persistent.getRange().upperEndpoint()).isEqualTo(7.0);
    }
}
