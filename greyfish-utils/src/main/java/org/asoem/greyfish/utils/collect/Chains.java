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
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;


public final class Chains {

    private Chains() {
    }

    public static <T> Iterable<T> of(@Nullable final T root, final Function<? super T, ? extends T> nextElementFunction) {
        checkNotNull(nextElementFunction);

        if (root == null) {
            return ImmutableList.of();
        } else {
            return new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    return new AbstractIterator<T>() {
                        T next = root;

                        @Override
                        protected T computeNext() {
                            if (next == null) {
                                return endOfData();
                            } else {
                                final T ret = next;
                                next = nextElementFunction.apply(next);
                                return ret;
                            }
                        }
                    };
                }
            };
        }
    }
}
