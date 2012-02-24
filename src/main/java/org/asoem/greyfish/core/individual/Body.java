package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

public class Body extends AbstractAgentComponent {

    /**
     *
     * @param owner the agent which this body is part of
     */
    private Body(Agent owner) {
        this();
        setAgent(checkNotNull(owner));
    }
    
    public Body(Body body) {
        super(body.getName());
    }

    /**
     *
     * @param body The original
     * @param cloner The Cloner
     */
    private Body(Body body, DeepCloner cloner) {
        super(body, cloner);
    }

    /**
     * Default Constructor.
     */
    public Body() {
        super("body");
    }

    public static Body newInstance(Agent owner) {
        return new Body(owner);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new Body(this, cloner);
    }

    @Override
    public void accept(ComponentVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Iterable<AgentComponent> children() {
        return Collections.emptyList();
    }

}
