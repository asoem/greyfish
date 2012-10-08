package org.asoem.greyfish.core.simulation;

import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 06.07.12
 * Time: 16:43
 */
public final class Simulations {

    private Simulations() {}

    /**
     * Calls {@link Simulation#nextStep()} until the given {@code predicate} returns {@code false}
     *
     * @param predicate the {@code Predicate} which will be checked after each step
     */
    public static <T extends Simulation> void runWhile(T simulation, Predicate<? super T> predicate) {
        checkNotNull(predicate);

        while (predicate.apply(simulation)) {
            simulation.nextStep();
        }
    }

    public static void runFor(Simulation simulation, int steps) {
        for (int i = 0; i < steps; i++) {
            simulation.nextStep();
        }
    }
}
