package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.MotionObject2D;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 18:58
 */
public final class AgentInitializers {
    public static Initializer<? super Agent> projection(final MotionObject2D motionObject2D) {
        return new Initializer<Agent>() {
            @Override
            public void initialize(Agent agent) {
                agent.setProjection(motionObject2D);
            }
        };
    }
}
