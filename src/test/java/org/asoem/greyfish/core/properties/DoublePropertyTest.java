package org.asoem.greyfish.core.properties;

import com.google.inject.Inject;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.utils.persistence.Persister;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 17:09
 */
public class DoublePropertyTest {

    @Inject
    private GreyfishExpressionFactory expressionFactory;
    @Inject
    private Persister persister;

    public DoublePropertyTest() {
        CoreInjectorHolder.coreInjector().injectMembers(this);
    }

    @Test
    public void testPersistence() throws Exception {
        // given
        final DoubleProperty doubleProperty = DoubleProperty.with().name("test").lowerBound(3.0).upperBound(7.0).initialValue(4.0).build();
        
        // when
        final DoubleProperty persistent = Persisters.createCopy(doubleProperty, DoubleProperty.class, persister);
        
        // then
        assertThat(persistent.getName()).isEqualTo("test");
        assertThat(persistent.getInitialValue()).isEqualTo(4.0);
        assertThat(persistent.getRange().lowerEndpoint()).isEqualTo(3.0);
        assertThat(persistent.getRange().upperEndpoint()).isEqualTo(7.0);
    }
}
