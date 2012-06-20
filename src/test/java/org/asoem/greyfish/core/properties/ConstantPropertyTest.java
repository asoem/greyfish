package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.individual.Callback;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: christoph
 * Date: 29.05.12
 * Time: 11:45
 */
public class ConstantPropertyTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testLazyness() throws Exception {
        // given
        final Callback<ConstantProperty<Object>, Object> function = mock(Callback.class);
        given(function.apply(any(ConstantProperty.class), any(Map.class))).willReturn(mock(Object.class), mock(Object.class));
        final ConstantProperty<Object> constantProperty = ConstantProperty.builder().callback(function).build();

        // when
        constantProperty.initialize();
        final Object value1 = constantProperty.getValue();
        final Object value2 = constantProperty.getValue();

        // then
        verify(function, times(1)).apply(any(ConstantProperty.class), any(Map.class));
        assertThat(value2).isEqualTo(value1);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInitialization() throws Exception {
        // given
        final Callback<ConstantProperty<Object>, Object> function = mock(Callback.class);
        given(function.apply(any(ConstantProperty.class), any(Map.class))).willReturn(mock(Object.class), mock(Object.class));
        final ConstantProperty<Object> constantProperty = ConstantProperty.builder().callback(function).build();

        // when
        constantProperty.initialize();
        final Object value1 = constantProperty.getValue();
        constantProperty.initialize();
        final Object value2 = constantProperty.getValue();

        // then
        verify(function, times(2)).apply(any(ConstantProperty.class), any(Map.class));
        assertThat(value2).isNotEqualTo(value1);
    }
}
