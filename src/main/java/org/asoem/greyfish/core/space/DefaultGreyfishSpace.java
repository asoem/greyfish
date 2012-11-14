package org.asoem.greyfish.core.space;

import org.asoem.greyfish.core.agent.DefaultGreyfishAgent;
import org.asoem.greyfish.utils.space.Point2D;

/**
 * User: christoph
 * Date: 14.11.12
 * Time: 14:39
 */
public class DefaultGreyfishSpace extends ForwardingSpace2D<DefaultGreyfishAgent, Point2D> {
    private final Space2D<DefaultGreyfishAgent, Point2D> space;

    public DefaultGreyfishSpace(Space2D<DefaultGreyfishAgent, Point2D> space) {
        this.space = space;
    }

    @Override
    protected Space2D<DefaultGreyfishAgent, Point2D> delegate() {
        return space;
    }
}
