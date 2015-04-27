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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.math.statistics.Samplings;
import org.junit.Test;

import javax.annotation.Nullable;

public class UnrolledList7AT extends UnrolledListAT {
    @Test
    public void testFindFirst() throws Exception {
        final int runs = 1000;
        final FunctionalList<String> functionalList = new UnrolledList7<>(ImmutableList.of("a", "b", "c", "d", "e", "f", "g"));
        final ImmutableList<String> immutableList = ImmutableList.copyOf(functionalList);
        final int predicateCount = 10000;
        final Iterable<String> toFind = Samplings.random(RandomGenerators.rng()).withReplacement().sample(immutableList, predicateCount);
        final Iterable<Predicate<String>> predicates = Iterables.transform(
                toFind,
                new Function<String, Predicate<String>>() {
                    @Nullable
                    @Override
                    public Predicate<String> apply(@Nullable final String input) {
                        return Predicates.equalTo(input);
                    }
                });

        testFindFirst(runs, immutableList, functionalList, predicates);
    }
}
