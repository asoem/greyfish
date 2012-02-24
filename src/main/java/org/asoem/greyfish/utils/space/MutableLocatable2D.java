package org.asoem.greyfish.utils.space;

import org.simpleframework.xml.ElementArray;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 24.02.12
 * Time: 10:05
 */
public class MutableLocatable2D implements Locatable2D {

    @ElementArray(name = "coordinates")
    private final double[] coordinates;

    public MutableLocatable2D() {
        coordinates = new double[2];
    }

    public MutableLocatable2D(double[] coordinates) {
        checkArgument(coordinates.length == 2);
        this.coordinates = coordinates;
    }

    public MutableLocatable2D(double x, double y) {
        this.coordinates = new double[] {x, y};
    }

    @Override
    public double getX() {
        return coordinates[0];
    }

    public void setX(double x) {
        coordinates[0] = x;
    }

    @Override
    public double getY() {
        return coordinates[1];
    }

    public void setY(double y) {
        coordinates[1] = y;
    }

    @Override
    public double[] getCoordinates() {
        return coordinates;
    }
}
