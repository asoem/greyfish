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
