package org.asoem.greyfish.utils.space;

import org.simpleframework.xml.Attribute;

/**
 * User: christoph
 * Date: 03.07.12
 * Time: 12:36
 */
public class MotionObject2DImpl extends ImmutableObject2D implements MotionObject2D {

    @Attribute(name = "collision")
    private final boolean collision;

    private MotionObject2DImpl(@Attribute(name = "x") double x,
                               @Attribute(name = "y") double y,
                               @Attribute(name = "orientation") double orientation,
                               @Attribute(name = "collision") boolean collision) {
        super(x, y, orientation);
        this.collision = collision;
    }

    @Override
    public boolean didCollide() {
        return collision;
    }

    public static MotionObject2D of(double x, double y, double newOrientation, boolean b) {
        return new MotionObject2DImpl(x, y, newOrientation, b);
    }
}
