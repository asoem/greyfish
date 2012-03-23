package org.asoem.greyfish.examples;

import com.google.common.base.Predicate;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.ClonalReproductionAction;
import org.asoem.greyfish.core.actions.DeathAction;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Avatar;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import javax.annotation.Nullable;

import static org.asoem.greyfish.core.conditions.GreyfishExpressionCondition.evaluate;
import static org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder.compile;
import static org.asoem.greyfish.utils.math.RandomUtils.nextDouble;

/**
 * User: christoph
 * Date: 22.03.12
 * Time: 14:14
 */
public class SimpleAsexualPopulation {
    public SimpleAsexualPopulation() {

        CoreInjectorHolder.coreInjector();

        Agent prototype = ImmutableAgent.of(Population.named("AsexualPopulation"))
                .addActions(
                        ClonalReproductionAction.with()
                                .name("clone")
                                .nClones(compile("1"))
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("rand:nextDouble() < 1.0 - $('simulation.agentCount') / 1000.0")),
                                        evaluate(compile("$('#clone.stepsSinceLastExecution') >= 10"))))
                                .build(),
                        DeathAction.with()
                                .name("die")
                                .executesIf(evaluate(compile("$('this.agent.age') >= 100")))
                                .build())
                .build();

        TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleAsexualPopulation", tiledSpace);
        for (int i = 0; i<10; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }

        ParallelizedSimulation.runScenario(scenarioBuilder.build(), new Predicate<ParallelizedSimulation>() {
            @Override
            public boolean apply(@Nullable ParallelizedSimulation parallelizedSimulation) {
                assert parallelizedSimulation != null;
                System.out.println(parallelizedSimulation.countAgents());
                return parallelizedSimulation.countAgents() == 0 || parallelizedSimulation.getSteps() == 100000;
            }
        });
    }

    public static void main(String[] args) {
        new SimpleAsexualPopulation();
    }
}
