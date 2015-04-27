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

package org.asoem.greyfish.utils.collect;

import org.asoem.greyfish.utils.persistence.Persisters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class FunctionalFifoBufferTest {
    @Test
    public void testSerialization() throws Exception {
        // given
        final FunctionalFifoBuffer<Integer> messageBox = FunctionalFifoBuffer.withCapacity(1);
        messageBox.add(1);

        // when
        final FunctionalFifoBuffer<Integer> copy = Persisters.copyAsync(messageBox, Persisters.javaSerialization());

        // then
        assertThat(copy, is(equalTo(messageBox)));
    }
}
