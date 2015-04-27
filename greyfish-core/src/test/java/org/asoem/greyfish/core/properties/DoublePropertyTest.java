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

package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.agent.Basic2DAgentContext;
import org.asoem.greyfish.utils.persistence.Persisters;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


public class DoublePropertyTest {

    @Test
    public void testPersistence() throws Exception {
        // given
        final DoubleProperty<Basic2DAgent, Basic2DAgentContext> doubleProperty = DoubleProperty.<Basic2DAgent, Basic2DAgentContext>with()
                .name("test")
                .lowerBound(3.0)
                .upperBound(7.0)
                .initialValue(4.0)
                .build();

        // when
        final DoubleProperty<Basic2DAgent, Basic2DAgentContext> persistent = Persisters.copyAsync(doubleProperty, Persisters.javaSerialization());

        // then
        MatcherAssert.assertThat(persistent, is(equalTo(doubleProperty)));
    }
}
