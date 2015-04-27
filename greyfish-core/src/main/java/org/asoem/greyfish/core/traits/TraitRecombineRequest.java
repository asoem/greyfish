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

package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.ComponentMessage;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TraitRecombineRequest<T> implements ComponentMessage {
    private final String name;
    private final T value1;
    private final T value2;

    public TraitRecombineRequest(final String name, final T value1, final T value2) {
        this.name = checkNotNull(name);
        this.value1 = checkNotNull(value1);
        this.value2 = checkNotNull(value2);
    }

    @Override
    public String componentName() {
        return name;
    }

    public T getValue1() {
        return value1;
    }

    public T getValue2() {
        return value2;
    }
}
