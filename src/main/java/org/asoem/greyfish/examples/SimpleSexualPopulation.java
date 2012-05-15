package org.asoem.greyfish.examples;

import com.google.common.base.Function;
import com.google.common.base.Functions;
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
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.FunctionProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import javax.annotation.Nullable;
import java.util.Map;

import static org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder.compile;
import static org.asoem.greyfish.core.individual.Callbacks.constant;
import static org.asoem.greyfish.utils.math.RandomUtils.nextDouble;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 14:36
 */
public class SimpleSexualPopulation {
    private final DescriptiveStatistics populationCountStatistics = new DescriptiveStatistics();
    private final DescriptiveStatistics stepsPerSecondStatistics = new DescriptiveStatistics();

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
                    stepsPerSecondStatistics.addValue(parallelizedSimulation.getStep() - lastStep);
                    millies = l;
                    lastStep = parallelizedSimulation.getStep();
                }
                return parallelizedSimulation.countAgents(Population.named("SexualPopulation")) == 0 || parallelizedSimulation.getStep() == 5000;
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
                                .provides(new Callback<ResourceProvisionAction, Double>() {
                                    @Override
                                    public Double apply(ResourceProvisionAction caller, Map<? super String, ?> localVariables) {
                                        final Double resourceValue = (Double) (caller.agent().getProperty("resource", FunctionProperty.class).getValue());
                                        final Double resource_classification = (Double) (caller.agent().getProperty("resource_classification", FunctionProperty.class).getValue());
                                        final Double classifier = (Double) localVariables.get("classifier");
                                        return Math.min(resourceValue, 10) * (1 - Math.abs(classifier - resource_classification));
                                    }
                                })
                                .build())
                .addProperties(
                        FunctionProperty.<Double>builder()
                                .name("resource")
                                .function(new Function<FunctionProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(FunctionProperty<Double> property) {
                                        return 1000 - property.agent().getAction("give", ResourceProvisionAction.class).getProvidedAmount();
                                    }
                                })
                                .build(),
                        FunctionProperty.<Double>builder()
                                .name("resource_classification")
                                .function(Functions.constant(0.5))
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
                                        return (Double) functionCondition.agent().getProperty("energy2", FunctionProperty.class).getValue() < 1.0;
                                    }
                                }))
                                .build(),
                        SexualReproductionAction.with()
                                .name("reproduce")
                                .clutchSize(compile("1"))
                                .spermStorage(compile("$('#receive').getReceivedSperm()"))
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getValue().equals("FEMALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() > 0;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return RandomUtils.nextDouble() < 1.0 - condition.agent().getSimulationContext().getSimulation().countAgents("SexualPopulation") / 900.0;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("reproduce", SexualReproductionAction.class).stepsSinceLastExecution() >= 10;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return (Double) condition.agent().getProperty("energy2", FunctionProperty.class).getValue() >= 10.0;
                                            }
                                        })))
                                .onSuccess(new Callback<AbstractGFAction, Void>() {
                                    @Override
                                    public Void apply(AbstractGFAction caller, Map<? super String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(10.0); return null;
                                    }
                                })
                                .build(),
                        MatingTransmitterAction.with()
                                .name("fertilize")
                                .ontology("mate")
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getValue().equals("MALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("fertilize", MatingTransmitterAction.class).getMatingCount() == 0;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return (Double) condition.agent().getProperty("energy2", FunctionProperty.class).getValue() >= 1.0;
                                            }
                                        })))
                                .onSuccess(new Callback<AbstractGFAction, Void>() {
                                    @Override
                                    public Void apply(AbstractGFAction caller, Map<? super String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(1.0); return null;
                                    }
                                })
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(constant(1.0))
                                .matingProbability(compile("1 - abs(mate.getComponent('consumer_classification').getValue() - $('#female_mating_preference').getValue())"))
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getValue().equals("FEMALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() == 0;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return (Double) condition.agent().getProperty("energy2", FunctionProperty.class).getValue() >= 1.0;
                                            }
                                        })))
                                .onSuccess(new Callback<AbstractGFAction, Void>() {
                                    @Override
                                    public Void apply(AbstractGFAction caller, Map<? super String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(1.0); return null;
                                    }
                                })
                                .build(),
                        ResourceConsumptionAction.with()
                                .name("consume")
                                .interactionRadius(compile("1"))
                                .ontology("energy")
                                .requestAmount(compile("10"))
                                .uptakeUtilization(compile("$('#energy').add(offer)")) // do nothing
                                .classification(compile("$('#consumer_classification').getValue()"))
                                .executesIf(new FunctionCondition(new Function<FunctionCondition, Boolean>() {
                                    @Override
                                    public Boolean apply(FunctionCondition functionCondition) {
                                        return functionCondition.agent().getAction("consume", ResourceConsumptionAction.class).stepsSinceLastExecution() >= 10;
                                    }
                                }))
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
                                .initialValue(compile("runif(0.4, 0.6)"))
                                .mutation(compile("rnorm(0, 0.1)"))
                                .build())
                .addProperties(
                        DoubleProperty.with()
                                .name("energy")
                                .initialValue(100.0)
                                .lowerBound(0.0)
                                .upperBound(100.0)
                                .build(),
                        FunctionProperty.<Double>builder()
                                .name("energy2")
                                .function(new Function<FunctionProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(FunctionProperty<Double> property) {
                                        return property.agent().getProperty("energy", DoubleProperty.class).getValue()
                                                - property.agent().getAge();
                                    }
                                })
                                .build()
                )
                .build();
    }

    public static void main(String[] args) {
        Guice.createInjector(new CoreModule())
                .getInstance(SimpleSexualPopulation.class);
    }
}
