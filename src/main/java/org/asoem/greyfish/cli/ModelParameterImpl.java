package org.asoem.greyfish.cli;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 15:18
 */
public class ModelParameterImpl implements ModelParameter, Serializable {

    private final String value;

    public ModelParameterImpl(String value) {
        this.value = checkNotNull(value, "value");
    }

    public String value() {
        return this.value;
    }

    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ModelParameter)) {
            return false;
        }

        ModelParameter other = (ModelParameter) o;
        return value.equals(other.value());
    }

    public String toString() {
        return "@" + ModelParameter.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType() {
        return ModelParameter.class;
    }

    private static final long serialVersionUID = 0;
}
