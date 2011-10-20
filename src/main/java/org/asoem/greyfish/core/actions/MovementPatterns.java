package org.asoem.greyfish.core.actions;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import static org.asoem.greyfish.utils.math.RandomUtils.*;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public class MovementPatterns {

    private static MovementPattern NO_MOVEMENT = new MovementPattern() {
        @Override
        public void apply(Agent agent, Simulation simulation) { /* DO NOTHING*/ }
    };

    public static MovementPattern noMovement() {
        return NO_MOVEMENT;
    }


    public static MovementPattern randomMovement(final double speed, final double rotationProbability) {
        return new MovementPattern() {
            @Override
            public void apply(Agent agent, Simulation simulation) {
                double angle = agent.getMotionVector().getAngle();

                if (trueWithProbability(rotationProbability)) {
                    angle = nextDouble(0, 10);
                }

                agent.setMotion(angle, speed);
            }
        };
    }

    public static MovementPattern borderAvoidanceMovement(final double speed, final double rotationProbability) {
        return new MovementPattern() {
            @Override
            public void apply(Agent agent, Simulation simulation) {
                double angle = agent.getMotionVector().getAngle();

                if (trueWithProbability(rotationProbability)) {
                    angle += (nextBoolean()) ?  0.3 : -0.3;
                }

                agent.setMotion(angle, speed);

                // check Border
                // TODO: LOD violation
                if (simulation.getSpace().checkForBorderCollision(agent, agent.getMotionVector())) {
                    agent.setMotion(angle + MathLib.PI / 4, speed);
                }
            }
        };
    }
}
