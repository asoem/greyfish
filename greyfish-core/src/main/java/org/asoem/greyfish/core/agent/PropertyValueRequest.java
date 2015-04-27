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

package org.asoem.greyfish.core.agent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A message to {@link Agent#ask(Object, Class) request} a trait value from an {@link
 * org.asoem.greyfish.core.agent.Agent agent}.
 */
public class PropertyValueRequest implements ComponentMessage {
    private final String name;

    public PropertyValueRequest(final String name) {
        this.name = checkNotNull(name);
    }

    @Override
    public String componentName() {
        return name;
    }
}
