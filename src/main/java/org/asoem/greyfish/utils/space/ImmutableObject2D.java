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

    @Element(name = "coordinates")
    private final Coordinates2D coordinates;

    @SimpleXMLConstructor
    private ImmutableObject2D(@Element(name = "coordinates") Coordinates2D coordinates,
                              @Element(name = "orientation") double orientation) {
        this.coordinates = coordinates;
        this.orientation = orientation;
    }

    @Override
    public double getOrientation() {
        return orientation;
    }

    @Override
    public Coordinates2D getCoordinates() {
        return coordinates;
    }

    public static ImmutableObject2D of(Coordinates2D coordinates, double orientationAngle) {
        checkNotNull(coordinates);
        return new ImmutableObject2D(coordinates, orientationAngle);
    }

    public static ImmutableObject2D copyOf(Object2D object2D) {
        return new ImmutableObject2D(object2D.getCoordinates(), object2D.getOrientation());
    }

    public static Object2D of(double v, double v1, double v2) {
        return new ImmutableObject2D(ImmutableCoordinates2D.at(v, v1), v2);
    }
}
