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
}
