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
 * Time: 17:15
 */
public class ConditionalStatesPropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        ConditionalStatesProperty<DefaultGreyfishAgent> statesProperty = ConditionalStatesProperty.<DefaultGreyfishAgent>with().name("test")
                .addState("A", "true").build();
        
        // when
        ConditionalStatesProperty<DefaultGreyfishAgent> persistent = Persisters.createCopy(statesProperty, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(persistent, is(equalTo(statesProperty)));
    }
}
