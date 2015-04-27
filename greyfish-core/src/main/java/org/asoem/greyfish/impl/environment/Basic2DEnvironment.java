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

package org.asoem.greyfish.impl.environment;

import org.asoem.greyfish.core.environment.SpatialEnvironment2D;
import org.asoem.greyfish.impl.agent.Basic2DAgent;
import org.asoem.greyfish.impl.space.BasicTiled2DSpace;
import org.asoem.greyfish.utils.space.Point2D;

public interface Basic2DEnvironment extends SpatialEnvironment2D<Basic2DAgent, BasicTiled2DSpace>, SynchronizedAgentsEnvironment<Basic2DAgent> {

    void enqueueAddition(Basic2DAgent agent, Point2D point2D);
}
