/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.space;

public class MutableMotion2D implements Motion2D {

    private ImmutableMotion2D motion2D = new ImmutableMotion2D(0, 0);

    public MutableMotion2D() {}

    public void setRotation(final double alpha) {
        setMotion(alpha % Math.PI, getTranslation());
    }

    public void setTranslation(final double translation) {
        setMotion(getRotation(), translation);
    }

    @Override
    public int getDimension() {
        return 2;
    }

    @Override
    public double getTranslation() {
        return motion2D.getTranslation();
    }

    @Override
    public double[] getRotationAngles() {
        return motion2D.getRotationAngles();
    }

    @Override
    public double getRotation() {
        return motion2D.getRotation();
    }

    /**
     * Change this object's motion vector by rotating it {@code angle}*PI degrees
     * and adding {@code velocity} to it's length
     * @param angle The value to sum to the angle of this object's motion vector
     * @param velocity The value to sum to the length of this object's motion vector
     */
    public void changeMotion(final double angle, final double velocity) {
        motion2D = motion2D.modified(angle, velocity);
    }

    /**
     * Change this object's motion vector by rotating it's orientation to {@code angle}*PI degrees
     * and it's length to {@code velocity}
     * @param angle The new value of the angle of this object's motion vector
     * @param velocity The new value of the length of this object's motion vector
     */
    public void setMotion(final double angle, final double velocity) {
        motion2D = ImmutableMotion2D.of(angle, velocity);
    }
}
