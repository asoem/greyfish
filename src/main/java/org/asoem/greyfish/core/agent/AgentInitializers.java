package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.Initializer;
import org.asoem.greyfish.utils.space.Object2D;

/**
 * User: christoph
 * Date: 03.10.12
 * Time: 18:58
 */
public final class AgentInitializers {

    private AgentInitializers() {}

    public static <P extends Object2D> Initializer<? super SpatialAgent<?, ?, P>> projection(final P projection) {
        return new Initializer<SpatialAgent<?, ?, P>>() {
            @Override
            public void initialize(SpatialAgent<?, ?, P> agent) {
                agent.setProjection(projection);
            }
        };
    }
}
