package org.asoem.greyfish.core.agent;

public final class PropertySetRequest implements ComponentMessage {
    private String name;
    private final Object object;

    public PropertySetRequest(final String name, final Object object) {
        this.name = name;
        this.object = object;
    }

    @Override
    public String componentName() {
        return name;
    }

    public Object getObject() {
        return object;
    }
}
