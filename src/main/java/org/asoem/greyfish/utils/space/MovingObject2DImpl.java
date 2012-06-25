package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 22.06.12
 * Time: 11:47
 */
public class MovingObject2DImpl implements MovingObject2D {
    private Object2D projection;
    private Motion2D motion;

    @Override
    public Object2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(Object2D projection) {
        this.projection = projection;
    }

    @Override
    public void collision(MovingObject2D other) {
        // nop
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public void setMotion(Motion2D motion) {
        this.motion = motion;
    }
}
