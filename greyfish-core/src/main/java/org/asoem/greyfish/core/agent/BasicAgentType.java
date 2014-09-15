package org.asoem.greyfish.core.agent;

public final class BasicAgentType implements AgentType {
    private final String name;

    public BasicAgentType(final String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }
}
