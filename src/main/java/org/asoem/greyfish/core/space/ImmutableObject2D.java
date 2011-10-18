package org.asoem.greyfish.core.space;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 18.10.11
 * Time: 16:26
 */
public class ImmutableObject2D implements Object2D {
    private final double orientation;
    private final Coordinates2D coordinates;

    private ImmutableObject2D(Coordinates2D coordinates, double orientation) {
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

    public static ImmutableObject2D of(Coordinates2D coordinates, int orientationAngle) {
        checkNotNull(coordinates);
        return new ImmutableObject2D(coordinates, orientationAngle);
    }
}
