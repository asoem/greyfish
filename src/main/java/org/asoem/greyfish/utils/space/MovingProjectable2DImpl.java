package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 22.06.12
 * Time: 11:47
 */
public class MovingProjectable2DImpl implements MovingProjectable2D {
    private MotionObject2D projection;
    private Motion2D motion;

    @Override
    public MotionObject2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(MotionObject2D projection) {
        this.projection = projection;
    }

    @Override
    public void collision(MovingProjectable2D other) {
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
