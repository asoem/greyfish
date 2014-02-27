package org.asoem.greyfish.core.model;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ModelParametersTest {
    @Test
    public void testBind() throws Exception {
        // given
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                ModelParameters.bind(binder(), ImmutableMap.of("a", "test", "b", "42", "c", "56.3"));
            }
        });

        // when
        final InjectableClass instance = injector.getInstance(InjectableClass.class);

        // then
        assertThat(instance.aString, is(equalTo("test")));
        assertThat(instance.anInt, is(equalTo(42)));
        assertThat(instance.aDouble, is(equalTo(56.3)));
        assertThat(instance.namedDouble, is(equalTo(56.3)));
    }

    private static class InjectableClass {
        private final String aString;
        private final int anInt;
        private final Double aDouble;
        private final Double namedDouble;

        @Inject
        private InjectableClass(@ModelParameter("a") String aString,
                                @ModelParameter("b") int anInt,
                                @ModelParameter("c") Double aDouble,
                                @Named("c") Double namedDouble) {
            this.aString = aString;
            this.anInt = anInt;
            this.aDouble = aDouble;
            this.namedDouble = namedDouble;
        }
    }
}
