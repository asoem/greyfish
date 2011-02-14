package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.RandomUtils;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public class MovementPatterns {

    private static MovementPattern NO_MOVEMENT = new MovementPattern() {
        @Override
        public void apply(Agent agent) { /* DO NOTHING*/ }
    };

    public static MovementPattern noMovement() {
        return NO_MOVEMENT;
    }


    public static MovementPattern randomMovement(final double speed) {
        return new MovementPattern() {
            @Override
            public void apply(Agent agent) {
                Simulation simulation = agent.getSimulation();
                if (RandomUtils.nextBoolean()) {
                    float phi = RandomUtils.nextFloat(0f, 0.1f);
                    simulation.rotate(agent, phi);
                }

                simulation.translate(agent, speed);
            }
        };
    }
}
