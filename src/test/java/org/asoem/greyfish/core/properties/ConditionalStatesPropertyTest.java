package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.io.persistence.JavaPersister;
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
        ConditionalStatesProperty<A> statesProperty = ConditionalStatesProperty.with().addState("A", "true").build();
        
        // when
        ConditionalStatesProperty<A> persistent = Persisters.createCopy(statesProperty, JavaPersister.INSTANCE);

        // then
        MatcherAssert.assertThat(persistent, is(equalTo(statesProperty)));
    }
}
