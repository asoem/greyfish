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


public class MovingProjectable2DImpl implements MovingProjectable2D {
    private MotionObject2D projection;
    private Motion2D motion;

    @Override
    public MotionObject2D getProjection() {
        return projection;
    }

    @Override
    public void setProjection(final MotionObject2D projection) {
        this.projection = projection;
    }

    @Override
    public Motion2D getMotion() {
        return motion;
    }

    @Override
    public void setMotion(final Motion2D motion) {
        this.motion = motion;
    }
}
