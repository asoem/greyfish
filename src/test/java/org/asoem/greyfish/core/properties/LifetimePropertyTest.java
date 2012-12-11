package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.core.io.persistence.JavaPersister;
import org.asoem.greyfish.utils.base.Arguments;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 29.05.12
 * Time: 11:45
 */
public class LifetimePropertyTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testLazyness() throws Exception {
        // given
        final Callback<LifetimeProperty<DefaultGreyfishAgent, Object>, Object> function = mock(Callback.class);
        given(function.apply(any(LifetimeProperty.class), any(Arguments.class))).willReturn(mock(Object.class), mock(Object.class));
        final LifetimeProperty<DefaultGreyfishAgent, Object> lifetimeProperty = LifetimeProperty.<Object, DefaultGreyfishAgent>builder().initialization(function).build();

        // when
        lifetimeProperty.initialize();
        final Object value1 = lifetimeProperty.getValue();
        final Object value2 = lifetimeProperty.getValue();

        // then
        verify(function, times(1)).apply(any(LifetimeProperty.class), any(Arguments.class));
        assertThat(value2, is(equalTo(value1)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitialization() throws Exception {
        // given
        final Callback<LifetimeProperty<DefaultGreyfishAgent, Object>, Object> function = mock(Callback.class);
        given(function.apply(any(LifetimeProperty.class), any(Arguments.class))).willReturn(mock(Object.class));
        final LifetimeProperty<DefaultGreyfishAgent, Object> lifetimeProperty = LifetimeProperty.<Object, DefaultGreyfishAgent>builder().initialization(function).build();

        // when
        lifetimeProperty.initialize();
        final Object value1 = lifetimeProperty.getValue();
        lifetimeProperty.initialize();
        final Object value2 = lifetimeProperty.getValue();

        // then
        verify(function, times(2)).apply(any(LifetimeProperty.class), any(Arguments.class));
        assertThat(value2, is(equalTo(value1)));
    }

    @Test
    public void testSerialization() throws Exception {
        // given
        final LifetimeProperty<DefaultGreyfishAgent, Integer> property = LifetimeProperty.<Integer, DefaultGreyfishAgent>builder()
                .name("foo")
                .initialization(Callbacks.constant(42))
                .build();

        // when
        final LifetimeProperty<DefaultGreyfishAgent, Integer> copy = Persisters.createCopy(property, JavaPersister.INSTANCE);

        // then
        assertThat(copy.getName(), is(equalTo(property.getName())));
        assertThat(copy.getInitializer(), is(Matchers.<Callback<? super LifetimeProperty<DefaultGreyfishAgent, Integer>, Integer>>equalTo(property.getInitializer())));
    }
}
