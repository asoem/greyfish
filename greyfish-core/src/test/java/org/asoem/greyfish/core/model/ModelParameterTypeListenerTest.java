package org.asoem.greyfish.core.model;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;


public class ModelParameterTypeListenerTest {

    @Test
    public void testInjection() throws Exception {
        // given
        final int newFieldValue = 1;
        final String newFiledValueStr = String.valueOf(newFieldValue);
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                final ModelParameterTypeListener modelParameterTypeListener =
                        new ModelParameterTypeListener(ImmutableMap.of("field", newFiledValueStr));
                bindListener(Matchers.any(), modelParameterTypeListener);
            }
        });

        // when
        final TypeParameterOwner instance = injector.getInstance(TypeParameterOwner.class);

        // then
        assertThat(instance, new CustomTypeSafeMatcher<TypeParameterOwner>("got it's fields injected") {
            @Override
            protected boolean matchesSafely(TypeParameterOwner item) {
                return item.field == newFieldValue;
            }
        });
    }

    private final static class TypeParameterOwner {
        @ModelParameter
        private int field = 0;
    }
}
