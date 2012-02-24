package org.asoem.greyfish.core.actions.utils;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;

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
                double rotationAngle = agent.getMotion().getRotation2D();

                if (trueWithProbability(rotationProbability)) {
                    rotationAngle = nextDouble(0, 10);
                }

                agent.setMotion(ImmutableMotion2D.of(rotationAngle, speed));
            }
        };
    }

    public static MovementPattern borderAvoidanceMovement(final double speed, final double rotationProbability) {
        return new MovementPattern() {
            @Override
            public void apply(Agent agent, Simulation simulation) {
                final Motion2D currentMotion = agent.getMotion();

                double newRotation = currentMotion.getRotation2D();
                double newTranslation = currentMotion.getTranslation();

                if (trueWithProbability(rotationProbability)) {
                    newRotation = (nextBoolean()) ?  0.3 : -0.3;
                }

                // TODO: LawOfDemeter violation
                if (simulation.getSpace().planMovement(agent, currentMotion).willCollide()) {
                    double rotation = currentMotion.getRotation2D();
                    newRotation = rotation + (rotation > 0 ? 0.1 : -0.1);
                }

                agent.setMotion(ImmutableMotion2D.of(newRotation, newTranslation));
            }
        };
    }
}
