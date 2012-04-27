package org.asoem.greyfish.examples;

import com.google.common.base.Predicate;
import com.google.common.primitives.Doubles;
import com.google.inject.Guice;
import javolution.lang.MathLib;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.core.actions.DeathAction;
import org.asoem.greyfish.core.actions.MatingReceiverAction;
import org.asoem.greyfish.core.actions.MatingTransmitterAction;
import org.asoem.greyfish.core.actions.SexualReproductionAction;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Avatar;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import javax.annotation.Nullable;

import static org.asoem.greyfish.core.conditions.GreyfishExpressionCondition.evaluate;
import static org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder.compile;
import static org.asoem.greyfish.utils.math.RandomUtils.nextDouble;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 14:36
 */
public class SimpleSexualPopulation {
    DescriptiveStatistics populationCountStatistics = new DescriptiveStatistics();
    DescriptiveStatistics stepsPerSecondStatistics = new DescriptiveStatistics();

    public SimpleSexualPopulation() {
        final Agent prototype = ImmutableAgent.of(Population.named("SexualPopulation"))
                .addActions(
                        SexualReproductionAction.with()
                                .name("reproduce")
                                .clutchSize(compile("1"))
                                .spermStorage(compile("$('#receive').getReceivedSperm()"))
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender') == 'FEMALE'")),
                                        evaluate(compile("rand:nextDouble() < 1.0 - $('simulation.agentCount') / 900.0")),
                                        evaluate(compile("$('#reproduce.stepsSinceLastExecution') >= 10"))))
                                .build(),
                        MatingTransmitterAction.with()
                                .name("fertilize")
                                .ontology("mate")
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender') == 'MALE'")),
                                        evaluate(compile("$('#fertilize').getSuccessCount() == 0"))))
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(1.0)
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender') == 'FEMALE'")),
                                        evaluate(compile("$('#receive').getSuccessCount() == 0"))))
                                .build(),
                        DeathAction.with()
                                .name("die")
                                .executesIf(evaluate(compile("$('this.agent.age') >= 100")))
                                .build())
                .addGenes(
                        MarkovGeneComponent.builder()
                                .name("gender")
                                .markovChain(EvaluatingMarkovChain.parse(
                                        "MALE -> FEMALE: 0.5;" +
                                        "FEMALE -> MALE: 0.5",
                                        GreyfishExpressionFactoryHolder.get()))
                                .initialState(compile("rand:sample('MALE','FEMALE')"))
                                .build()
                )
                .build();

        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);
        for (int i = 0; i<10; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }

        final ParallelizedSimulation simulation = ParallelizedSimulation.runScenario(scenarioBuilder.build(), new Predicate<ParallelizedSimulation>() {

            private long millies = System.currentTimeMillis();
            int lastStep = -1;

            @Override
            public boolean apply(@Nullable ParallelizedSimulation parallelizedSimulation) {
                assert parallelizedSimulation != null;

                //System.out.println(parallelizedSimulation.countAgents());
                final long l = System.currentTimeMillis();
                if (l > millies + 1000) {
                    populationCountStatistics.addValue(parallelizedSimulation.countAgents());
                    stepsPerSecondStatistics.addValue(parallelizedSimulation.getCurrentStep() - lastStep);
                    millies = l;
                    lastStep = parallelizedSimulation.getCurrentStep();
                }
                return parallelizedSimulation.countAgents() == 0 || parallelizedSimulation.getCurrentStep() == 20000;
            }
        });

        simulation.shutdown();

        System.out.println(Doubles.join(" ", populationCountStatistics.getValues()));
        System.out.println(Doubles.join(" ", stepsPerSecondStatistics.getValues()));
    }

    public static void main(String[] args) {
        Guice.createInjector(new CoreModule())
                .getInstance(SimpleSexualPopulation.class);
    }
}
