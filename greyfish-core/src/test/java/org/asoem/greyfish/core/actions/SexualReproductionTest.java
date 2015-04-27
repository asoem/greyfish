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

package org.asoem.greyfish.core.actions;


public class SexualReproductionTest {

    /**
     @Test public void testSerialization() throws Exception {
     // given
     final SexualReproduction<Basic2DAgent> action = SexualReproduction.<Basic2DAgent>builder()
     .name("test")
     .clutchSize(Callbacks.constant(1))
     .spermSupplier(Callbacks.constant(Lists.<Chromosome>newArrayList()))
     .spermSelectionStrategy(ElementSelectionStrategies.<Chromosome>randomSelection())
     .spermFitnessCallback(Callbacks.constant(0.42))
     .onSuccess(Callbacks.emptyCallback())
     .executedIf(new AlwaysTrueCondition<Basic2DAgent>())
     .build();

     // when
     final SexualReproduction<Basic2DAgent> copy = Persisters.copyAsync(action, Persisters.javaSerialization());

     // then
     assertThat(copy, is(equalTo(action)));
     }
     **/
}
