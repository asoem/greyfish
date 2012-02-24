package org.asoem.greyfish.utils.space;

import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 16:26
 */
public class ImmutableObject2D implements Object2D {

    @Element(name = "orientation")
    private final double orientation;

    @Element(name = "locatable")
    private final Locatable2D locatable;

    @SimpleXMLConstructor
    private ImmutableObject2D(@Element(name = "locatable") Locatable2D locatable,
                              @Element(name = "orientation") double orientation) {
        this.locatable = locatable;
        this.orientation = orientation;
    }

    @Override
    public double getOrientationAngle() {
        return orientation;
    }

    public static ImmutableObject2D of(Locatable2D locatable, double orientationAngle) {
        checkNotNull(locatable);
        return new ImmutableObject2D(locatable, orientationAngle);
    }

    @Override
    public double getX() {
        return locatable.getX();
    }

    @Override
    public double getY() {
        return locatable.getY();
    }

    @Override
    public double[] getCoordinates() {
        return locatable.getCoordinates();
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public double[] getOrientation() {
        return new double[] {orientation};
    }

    @Override
    public double[] getBoundingVolume() {
        return new double[] {0, 0}; // todo: implement
    }
}
