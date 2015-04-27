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
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class ImmutableMapBuilderTest {

    @Test
    public void immutableMapBuilderTest() {
        // given
        final ImmutableMapBuilder<String, Integer> builder = ImmutableMapBuilder.newInstance();

        // when
        final Map<String, Integer> map = builder.putAll(ImmutableList.<String>of("A", "B"), Functions.<String>identity(), new Function<String, Integer>() {
            @Override
            public Integer apply(@Nullable final String s) {
                return 1;
            }
        }).build();

        // then
        assertEquals(ImmutableMap.of("A", 1, "B", 1), map);
    }
}
