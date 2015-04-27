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

package org.asoem.greyfish.core.model;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.environment.Environment;

import java.util.List;

public abstract class AbstractExperiment implements Experiment {
    private List<SimulationListener> listeners = Lists.newCopyOnWriteArrayList();

    @Override
    public final void addSimulationListener(final SimulationListener listener) {
        this.listeners.add(listener);
    }

    protected final void notifyStarted(final Environment<?> environment) {
        for (SimulationListener listener : listeners) {
            listener.started(environment);
        }
    }

    protected final void notifyDone(final Environment<?> environment) {
        for (SimulationListener listener : listeners) {
            listener.done(environment);
        }
    }
}
