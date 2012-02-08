package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import static org.asoem.greyfish.utils.math.RandomUtils.*;

/**
 * User: christoph
 * Date: 14.02.11
 * Time: 09:59
 */
public class MovementPatterns {

    private static final MovementPattern NO_MOVEMENT = new MovementPattern() {
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
                double rotationAngle = agent.getRotation();

                if (trueWithProbability(rotationProbability)) {
                    rotationAngle = nextDouble(0, 10);
                }

                agent.setMotion(rotationAngle, speed);
            }
        };
    }

    public static MovementPattern borderAvoidanceMovement(final double speed, final double rotationProbability) {
        return new MovementPattern() {
            @Override
            public void apply(Agent agent, Simulation simulation) {

                agent.setRotation(0);
                if (trueWithProbability(rotationProbability)) {
                    agent.setRotation((nextBoolean()) ?  0.3 : -0.3);
                }

                // TODO: LawOfDemeter violation
                if (simulation.getSpace().planMovement(agent).willCollide()) {
                    double rotation = agent.getRotation();
                    agent.setRotation(rotation + (rotation > 0 ? 0.1 : -0.1));
                }
            }
        };
    }
}
