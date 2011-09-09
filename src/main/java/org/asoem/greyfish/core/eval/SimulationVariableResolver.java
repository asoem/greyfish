package org.asoem.greyfish.core.eval;

import com.google.common.base.Functions;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 08.09.11
 * Time: 11:51
 */
class SimulationVariableResolver implements VariableResolver {

    final Simulation simulation;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationVariableResolver.class);

    public SimulationVariableResolver(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    public Object resolve(@Nonnull String arg0) {
                checkNotNull(arg0);

        LOGGER.trace("Resolving variable '{}' for agent {}", arg0, simulation);
        LOGGER.debug("Not implemented yet");
        // TODO: implement / migrate from AgentVariableResolver
        return Functions.<Object>constant(null);
    }
}
