package org.asoem.greyfish.examples;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.primitives.Doubles;
import com.google.inject.Guice;
import javolution.lang.MathLib;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.core.actions.*;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.conditions.FunctionCondition;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.Avatar;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ExpressionProperty;
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

        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);

        final Agent prototype = createConsumerPrototype();
        for (int i = 0; i<100; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }

        final Agent resource = createResourcePrototype();
        for (int i=0; i<20; ++i) {
            scenarioBuilder.addAgent(new Avatar(resource), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
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
                return parallelizedSimulation.countAgents(Population.named("SexualPopulation")) == 0 || parallelizedSimulation.getCurrentStep() == 20000;
            }
        });

        simulation.shutdown();

        System.out.println(Doubles.join(" ", populationCountStatistics.getValues()));
        System.out.println(Doubles.join(" ", stepsPerSecondStatistics.getValues()));
    }

    private static Agent createResourcePrototype() {
        return ImmutableAgent.of(Population.named("Resource"))
                .addActions(
                        ResourceProvisionAction.with()
                                .name("give")
                                .ontology("energy")
                                .provides(compile("min($('#resource').get().asDouble(), 10) * abs(classifier - $('#resource_classification').get().asDouble())"))
                                .build())
                .addProperties(
                        ExpressionProperty.with()
                                .name("resource")
                                .expression(compile("99 - $('#give').getProvidedAmount()"))
                                .build(),
                        ExpressionProperty.with()
                                .name("resource_classification")
                                .expression(compile("0.50"))
                                .build())
                .build();
    }

    private static Agent createConsumerPrototype() {
        return ImmutableAgent.of(Population.named("SexualPopulation"))
                .addActions(
                        DeathAction.with()
                                .name("die")
                                .executesIf(FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                    @Override
                                    public Boolean apply(FunctionCondition functionCondition) {
                                        return functionCondition.agent().getProperty("energy2", ExpressionProperty.class).evaluate().asDouble() < 1.0;
                                    }
                                }))
                                .build(),
                        SexualReproductionAction.with()
                                .name("reproduce")
                                .clutchSize(compile("1"))
                                .spermStorage(compile("$('#receive').getReceivedSperm()"))
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender').getValue() == 'FEMALE'")),
                                        evaluate(compile("rand:nextDouble() < 1.0 - $('simulation.agentCount') / 900.0")),
                                        evaluate(compile("$('#reproduce').stepsSinceLastExecution() >= 10")),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition functionCondition) {
                                                return functionCondition.agent().getProperty("energy2", ExpressionProperty.class).evaluate().asDouble() >= 10.0;
                                            }
                                        })))
                                .onSuccess(compile("$('#energy').subtract(10.0)"))
                                .build(),
                        MatingTransmitterAction.with()
                                .name("fertilize")
                                .ontology("mate")
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender').getValue() == 'MALE'")),
                                        evaluate(compile("$('#fertilize').getMatingCount() == 0")),
                                        evaluate(compile("$('#energy2').getValue() >= 1.0"))))
                                .onSuccess(compile("$('#energy').subtract(1.0)"))
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(1.0)
                                .matingProbability(compile("1 - abs(mate.getComponent('consumer_classification').getValue() - $('#consumer_classification').getValue())"))
                                .executesIf(AllCondition.evaluates(
                                        evaluate(compile("$('#gender').getValue() == 'FEMALE'")),
                                        evaluate(compile("$('#receive').getMatingCount() == 0")),
                                        evaluate(compile("$('#energy2').getValue() >= 1.0"))))
                                .onSuccess(compile("$('#energy').subtract(1.0)"))
                                .build(),
                        ResourceConsumptionAction.with()
                                .name("consume")
                                .interactionRadius(compile("1"))
                                .ontology("energy")
                                .requestAmount(compile("10"))
                                .uptakeUtilization(compile("$('#energy').add(offer)")) // do nothing
                                .classification(compile("$('#consumer_classification').getValue()"))
                                .executesIf(evaluate(compile("$('#consume').stepsSinceLastExecution() >= 10")))
                                .build(),
                        SimpleMovementAction.builder()
                                .name("move")
                                .build())
                .addGenes(
                        MarkovGeneComponent.builder()
                                .name("gender")
                                .markovChain(EvaluatingMarkovChain.parse(
                                        "MALE -> FEMALE: 0.5;" +
                                                "FEMALE -> MALE: 0.5",
                                        GreyfishExpressionFactoryHolder.get()))
                                .initialState(compile("rand:sample('MALE','FEMALE')"))
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialValue(compile("rnorm(0.5, 0.1)"))
                                .mutation(compile("rnorm(0, 0.1)"))
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("female_mating_preference")
                                .initialValue(compile("runif(0.0, 100.0)"))
                                .mutation(compile("rnorm(0, 0.1)"))
                                .build())
                .addProperties(
                        DoubleProperty.with()
                                .name("energy")
                                .initialValue(100.0)
                                .lowerBound(0.0)
                                .upperBound(100.0)
                                .build(),
                        ExpressionProperty.with()
                                .name("energy2")
                                .expression(compile("$('#energy').getValue() - $('this.agent.age')"))
                                .build()
                )
                .build();
    }

    public static void main(String[] args) {
        Guice.createInjector(new CoreModule())
                .getInstance(SimpleSexualPopulation.class);
    }
}
