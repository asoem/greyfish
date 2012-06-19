package org.asoem.greyfish.scenarios;

import com.google.common.base.Function;
import com.google.inject.Provider;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.*;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.conditions.FunctionCondition;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.properties.ConstantProperty;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.DynamicProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
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
public class SimpleSexualPopulation implements Provider<Scenario> {

    @Override
    public Scenario get() {
        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);

        final Agent prototype = createConsumerPrototype();
        for (int i = 0; i < 500; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }

        /*
        final Agent resource = createResourcePrototype();
        for (int i=0; i<20; ++i) {
            scenarioBuilder.addAgent(new Avatar(resource), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
        }
        */

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
                                    public Double apply(ResourceProvisionAction caller, Map<String, ?> arguments) {
                                        final Double resourceValue = (Double) (caller.agent().getProperty("resource", GFProperty.class).getValue());
                                        final Double resourceClassification = (Double) caller.agent().getProperty("resource_classification", GFProperty.class).getValue();
                                        final Double consumerClassification = (Double) arguments.get("classifier");
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
                        ConstantProperty.builder()
                                .name("resource_classification")
                                .function(new Function<ConstantProperty<Object>, Object>() {
                                    @Override
                                    public Object apply(@Nullable ConstantProperty<Object> objectConstantProperty) {
                                        return RandomUtils.sample(0.1, 0.9);
                                    }
                                })
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
                                    public List<? extends Chromosome> apply(SexualReproductionAction caller, Map<String, ?> arguments) {
                                        return caller.agent().getAction("receive", MatingReceiverAction.class).getReceivedSperm();
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
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
                                    public Void apply(AbstractGFAction caller, Map<String, ?> arguments) {
                                        caller.agent().getProperty("energy", DoubleProperty.class).subtract(100.0);
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
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("MALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAge() >= 300;
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
                                    public Void apply(AbstractGFAction caller, Map<String, ?> arguments) {
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
                                    public Double apply(MatingReceiverAction caller, Map<String, ?> arguments) {
                                        final Double classificationOfMate = (Double) ((Agent) arguments.get("mate")).getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final Double myClassification = (Double) caller.agent().getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final Double preferredSimilarity = Math.max(0.0, Math.min(1.0, caller.agent().getGene("preferred_similarity", DoubleGeneComponent.class).getAllele()));
                                        final double minimalSimilarity = 0.9;

                                        final boolean b = abs(classificationOfMate - myClassification) < Math.max(preferredSimilarity, minimalSimilarity);
                                        return (b) ? 1.0 : 0.0;
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAge() >= 300;
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
                                .build(),
                        /*
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
                                        return caller.agent().getGene("consumer_classification", GeneComponent.class).getValue();
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAge() >= 100 &&
                                                        condition.agent().getAge() < 300;
                                            }
                                        }),
                                        new FunctionCondition(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition functionCondition) {
                                                return functionCondition.agent().getAction("consume", ResourceConsumptionAction.class).stepsSinceLastExecution() >= 10;
                                            }
                                        })
                                ))
                                .build(),
                                */
                        GenericMovementAction.builder()
                                .name("move")
                                .stepSize(Callbacks.constant(0.1))
                                .turningAngle(new Callback<GenericMovementAction, Double>() {
                                    @Override
                                    public Double apply(GenericMovementAction caller, Map<String, ?> arguments) {
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
                                    public String apply(MarkovGeneComponent caller, Map<String, ?> arguments) {
                                        return RandomUtils.sample("MALE", "FEMALE");
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialAllele(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        return 0.1;
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        return ((Double) arguments.get("original")) + (trueWithProbability(0.0001) ? sample(-0.01, 0.01) : 0.0);
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("preferred_similarity")
                                .initialAllele(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        return 1.0;
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        return ((Double) arguments.get("original")) + (trueWithProbability(0.0001) ? sample(-0.01, 0.01) : 0.0);
                                    }
                                })
                                .build()
                )
                .addProperties(
                        DoubleProperty.with()
                                .name("energy")
                                .initialValue(500.0)
                                .lowerBound(0.0)
                                .upperBound(500.0)
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
