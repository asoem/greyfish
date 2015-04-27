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

import java.io.Serializable;
import java.lang.annotation.Annotation;

@SuppressWarnings("ClassExplicitlyAnnotation")
final class ModelParameterImpl implements ModelParameter, Serializable {
    private final String value;
    private final boolean optional;

    public ModelParameterImpl(final String value, final boolean optional) {
        this.value = value;
        this.optional = optional;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean optional() {
        return optional;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ModelParameter.class;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ModelParameter)) {
            return false;
        }

        final ModelParameter that = (ModelParameter) o;

        if (optional != that.optional()) {
            return false;
        }
        if (!value.equals(that.value())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return ((127 * "optional".hashCode()) ^ Boolean.valueOf(optional).hashCode())
                + ((127 * "value".hashCode()) ^ value.hashCode());
    }

    public String toString() {
        return "@" + ModelParameter.class.getName() + "(value=" + value + ", optional=" + optional + ")";
    }

    private static final long serialVersionUID = 0;
}
