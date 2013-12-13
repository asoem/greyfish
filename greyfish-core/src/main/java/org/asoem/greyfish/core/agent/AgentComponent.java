package org.asoem.greyfish.core.agent;


import java.io.Serializable;

public interface AgentComponent extends AgentNode, Serializable {

    /**
     * @return the name of this component
     */
    String getName();
}
