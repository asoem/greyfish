package org.asoem.greyfish.scenarios;

import com.google.inject.Provider;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.ClonalReproductionAction;
import org.asoem.greyfish.core.actions.DeathAction;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import java.util.Map;

import static org.asoem.greyfish.core.conditions.GreyfishExpressionCondition.evaluate;
import static org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder.compile;
import static org.asoem.greyfish.utils.math.RandomUtils.nextDouble;

/**
 * User: christoph
 * Date: 22.03.12
 * Time: 14:14
 */
public class SimpleAsexualPopulation implements Provider<Scenario> {

    @Override
    public Scenario get() {
        Agent prototype = ImmutableAgent.of(Population.named("AsexualPopulation"))
                .addActions(
                        ClonalReproductionAction.with()
                                .name("clone")
                                .nClones(compile("1"))
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("rand:nextDouble() < 1.0 - $('simulation.agentCount') / 900.0")),
                                        evaluate(compile("$('#clone.stepsSinceLastExecution') >= 10"))))
                                .build(),
                        DeathAction.with()
                                .name("die")
                                .executesIf(evaluate(compile("$('this.agent.age') >= 100")))
                                .build())
                .addGenes(
                        DoubleGeneComponent.builder()
                                .name("gene1")
                                .initialValue(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return 50.0 + RandomUtils.rnorm(0.0, 10.0);
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return ((Double) localVariables.get("original")) + RandomUtils.rnorm(0.0, 1.0);
                                    }
                                })
                                .build()
                )
                .build();

        TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleAsexualPopulation", tiledSpace);
        for (int i = 0; i < 10; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }

        return scenarioBuilder.build();
    }
}
