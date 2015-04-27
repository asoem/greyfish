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

package org.asoem.greyfish.cli;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import org.asoem.greyfish.core.model.Experiment;

/**
 * This service executes a given {@link org.asoem.greyfish.core.environment.DiscreteTimeEnvironment}.
 */
final class ExperimentExecutionService extends AbstractExecutionThreadService {

    private final Experiment experiment;

    @Inject
    private ExperimentExecutionService(final Experiment experiment, final EventBus eventBus) {
        this.experiment = experiment;
        eventBus.register(this);
    }

    @Override
    protected void run() throws Exception {
        experiment.run();
    }

    public Experiment getExperiment() {
        return experiment;
    }

    @Subscribe
    public void eventBusExceptionHandler(final Throwable throwable) {
        stopAsync();
    }
}
