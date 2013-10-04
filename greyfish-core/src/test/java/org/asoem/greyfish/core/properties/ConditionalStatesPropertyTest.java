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
 * Time: 17:15
 */
public class ConditionalStatesPropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final ConditionalStatesProperty<Basic2DAgent> statesProperty = ConditionalStatesProperty.<Basic2DAgent>with().name("test")
                .addState("A", "true").build();
        
        // when
        final ConditionalStatesProperty<Basic2DAgent> persistent = Persisters.copyAsync(statesProperty, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(persistent, is(equalTo(statesProperty)));
    }
}
