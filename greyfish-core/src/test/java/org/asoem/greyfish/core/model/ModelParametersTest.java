/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
