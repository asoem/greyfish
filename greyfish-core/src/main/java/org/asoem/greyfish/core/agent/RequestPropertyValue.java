package org.asoem.greyfish.core.agent;

public class RequestPropertyValue {
    private final String propertyName;

    public RequestPropertyValue(final String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
