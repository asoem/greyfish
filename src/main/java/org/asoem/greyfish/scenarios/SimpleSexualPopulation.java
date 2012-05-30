package org.asoem.greyfish.scenarios;

import com.google.common.base.Function;
import com.google.inject.Provider;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.*;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.conditions.FunctionCondition;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.properties.ConstantProperty;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.DynamicProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

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
public class SimpleSexualPopulation implements Provider<Scenario> {

    @Override
    public Scenario get() {
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

        return scenarioBuilder.build();
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
                                        final Double resourceValue = (Double) (caller.agent().getProperty("resource", DynamicProperty.class).getValue());
                                        final Double resourceClassification = caller.agent().getProperty("resource_classification", DoubleProperty.class).getValue();
                                        final Double consumerClassification = (Double) localVariables.get("classifier");
                                        final double dist = abs(consumerClassification - resourceClassification);
                                        final double v = 1 - (4 * dist) / sqrt(1 + pow(4 * dist, 2));
                                        return min(resourceValue, 100) * v;
                                    }
                                })
                                .build())
                .addProperties(
                        DynamicProperty.<Double>builder()
                                .name("resource")
                                .function(new Function<DynamicProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(DynamicProperty<Double> property) {
                                        return 10000 - property.agent().getAction("give", ResourceProvisionAction.class).getProvidedAmount();
                                    }
                                })
                                .build(),
                        DoubleProperty.with()
                                .name("resource_classification")
                                .lowerBound(0.0)
                                .upperBound(1.0)
                                .initialValue(RandomUtils.sample(0.1, 0.9))
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
                                        return (Double) functionCondition.agent().getProperty("energy2", DynamicProperty.class).getValue() < 1.0;
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
                                                return RandomUtils.nextDouble() < 1.0 - condition.agent().getSimulationContext().getSimulation().countAgents("SexualPopulation") / 2000.0;
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
                                                return (Double) condition.agent().getProperty("energy2", DynamicProperty.class).getValue() >= 30.0;
                                            }
                                        })))
                                .onSuccess(new Callback<AbstractGFAction, Void>() {
                                    @Override
                                    public Void apply(AbstractGFAction caller, Map<String, ?> localVariables) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(200.0);
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
                                                return (Double) condition.agent().getProperty("energy2", DynamicProperty.class).getValue() >= 1.0;
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
                                        final Double classificationOfMate = (Double) ((Agent) localVariables.get("mate")).getProperty("consumer_classification", ConstantProperty.class).getValue();
                                        final Double matingPreference = caller.agent().getGene("female_mating_preference", DoubleGeneComponent.class).getValue();
                                        // accept mates with min 90% similarity
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
                                                return (Double) condition.agent().getProperty("energy2", DynamicProperty.class).getValue() >= 1.0;
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
                                        return caller.agent().getProperty("consumer_classification", ConstantProperty.class).getValue();
                                    }
                                })
                                .executesIf(new FunctionCondition(new Function<FunctionCondition, Boolean>() {
                                    @Override
                                    public Boolean apply(FunctionCondition functionCondition) {
                                        return functionCondition.agent().getAction("consume", ResourceConsumptionAction.class).stepsSinceLastExecution() >= 10;
                                    }
                                }))
                                .build(),
                        GenericMovementAction.builder()
                                .name("move")
                                .stepSize(Callbacks.constant(0.1))
                                .turningAngle(new Callback<GenericMovementAction, Double>() {
                                    @Override
                                    public Double apply(GenericMovementAction caller, Map<String, ?> localVariables) {
                                        return caller.agent().getMotion().getRotation() + RandomUtils.rnorm(0, 0.08);
                                    }
                                })
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
                                .name("consumer_gene_1")
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
                                .name("consumer_gene_2")
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
                                .initialValue(2000.0)
                                .lowerBound(0.0)
                                .upperBound(2000.0)
                                .build(),
                        ConstantProperty.<Double>builder()
                                .name("consumer_classification")
                                .function(new Function<ConstantProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(ConstantProperty<Double> property) {
                                        return property.agent().getGene("consumer_gene_1", DoubleGeneComponent.class).getValue()
                                                + property.agent().getGene("consumer_gene_2", DoubleGeneComponent.class).getValue();
                                    }
                                })
                                .build(),
                        DynamicProperty.<Double>builder()
                                .name("energy2")
                                .function(new Function<DynamicProperty<Double>, Double>() {
                                    @Override
                                    public Double apply(DynamicProperty<Double> property) {
                                        return property.agent().getProperty("energy", DoubleProperty.class).getValue()
                                                - property.agent().getAge();
                                    }
                                })
                                .build()
                )
                .build();
    }
}
