package org.asoem.greyfish.cli;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 30.05.12
 * Time: 15:18
 */
public class ScenarioParameterImpl implements ScenarioParameter, Serializable {

    private final String value;

    public ScenarioParameterImpl(String value) {
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
        if (!(o instanceof ScenarioParameter)) {
            return false;
        }

        ScenarioParameter other = (ScenarioParameter) o;
        return value.equals(other.value());
    }

    public String toString() {
        return "@" + ScenarioParameter.class.getName() + "(value=" + value + ")";
    }

    public Class<? extends Annotation> annotationType() {
        return ScenarioParameter.class;
    }

    private static final long serialVersionUID = 0;
}
