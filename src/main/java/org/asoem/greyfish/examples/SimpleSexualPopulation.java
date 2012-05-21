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
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.inject.CoreModule;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.FunctionProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;
import static org.asoem.greyfish.core.individual.Callbacks.constant;
import static org.asoem.greyfish.utils.math.RandomUtils.*;

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
        for (int i = 0; i<500; ++i) {
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
                return parallelizedSimulation.countAgents(Population.named("SexualPopulation")) == 0 || parallelizedSimulation.getStep() == 20000;
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
                                    public Double apply(ResourceProvisionAction caller, Map<String, ?> localVariables) {
                                        final Double resourceValue = (Double) (caller.agent().getProperty("resource", FunctionProperty.class).getValue());
                                        final Double resource_classification = caller.agent().getProperty("resource_classification", DoubleProperty.class).getValue();
                                        final Double classifier = (Double) localVariables.get("classifier");
                                        final double dist = 1 - abs(classifier - resource_classification);
                                        final double v = 1 - (4 * dist) / (sqrt(1 + pow((4 * dist), 2)));
                                        return min(resourceValue, 100) * v;
                                    }
                                })
                                .build())
                .addProperties(
                        FunctionProperty.<Double>builder()
                                .name("resource")
                                .function(new Function<FunctionProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(FunctionProperty<Double> property) {
                                        return 10000 - property.agent().getAction("give", ResourceProvisionAction.class).getProvidedAmount();
                                    }
                                })
                                .build(),
                        DoubleProperty.with()
                                .name("resource_classification")
                                .lowerBound(0.0)
                                .upperBound(1.0)
                                .initialValue(RandomUtils.sample(0.2, 0.8))
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
                                .clutchSize(constant(1))
                                .spermSupplier(new Callback<SexualReproductionAction, List<? extends Chromosome>>() {
                                    @Override
                                    public List<? extends Chromosome> apply(SexualReproductionAction caller, Map<String, ?> localVariables) {
                                        return caller.agent().getAction("receive", MatingReceiverAction.class).getReceivedSperm();
                                    }
                                })
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
                                                return (Double) condition.agent().getProperty("energy2", FunctionProperty.class).getValue() >= 30.0;
                                            }
                                        })))
                                .onSuccess(new Callback<AbstractGFAction, Void>() {
                                    @Override
                                    public Void apply(AbstractGFAction caller, Map<String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(30.0);
                                        return null;
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
                                    public Void apply(AbstractGFAction caller, Map<String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(1.0);
                                        return null;
                                    }
                                })
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(constant(1.0))
                                .matingProbability(new Callback<MatingReceiverAction, Double>() {
                                    @Override
                                    public Double apply(MatingReceiverAction caller, Map<String, ?> localVariables) {
                                        final Double classificationOfMate = ((Agent) localVariables.get("mate")).getGene("consumer_classification", DoubleGeneComponent.class).getValue();
                                        final Double matingPreference = caller.agent().getGene("female_mating_preference", DoubleGeneComponent.class).getValue();
                                        final boolean b = abs(classificationOfMate - matingPreference) < 0.1;
                                        return (b) ? 1.0 : 0.0;
                                    }
                                })
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
                                    public Void apply(AbstractGFAction caller, Map<String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(1.0);
                                        return null;
                                    }
                                })
                                .build(),
                        ResourceConsumptionAction.with()
                                .name("consume")
                                .interactionRadius(constant(1.0))
                                .ontology("energy")
                                .requestAmount(constant(10.0))
                                .uptakeUtilization(new Callback<ResourceConsumptionAction, Void>() {
                                    @Override
                                    public Void apply(ResourceConsumptionAction caller, Map<String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).add((Double) localVariables.get("offer"));
                                        return null;
                                    }
                                })
                                .classification(new Callback<ResourceConsumptionAction, Object>() {
                                    @Override
                                    public Object apply(ResourceConsumptionAction caller, Map<String, ?> localVariables) {
                                        return caller.agent().getGene("consumer_classification", DoubleGeneComponent.class).getValue();
                                    }
                                })
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
                                .put("MALE", "FEMALE", Callbacks.constant(0.5))
                                .put("FEMALE", "MALE", Callbacks.constant(0.5))
                                .initialState(new Callback<MarkovGeneComponent, String>() {
                                    @Override
                                    public String apply(MarkovGeneComponent caller, Map<String, ?> localVariables) {
                                        return RandomUtils.sample("MALE", "FEMALE");
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialValue(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return runif(0.0, 1.0);
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return rnorm(0, 0.01);
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("female_mating_preference")
                                .initialValue(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return runif(0.0, 1.0);
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return rnorm(0, 0.01);
                                    }
                                })
                                .build())
                .addProperties(
                        DoubleProperty.with()
                                .name("energy")
                                .initialValue(200.0)
                                .lowerBound(0.0)
                                .upperBound(200.0)
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
