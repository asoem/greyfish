package org.asoem.greyfish.scenarios;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javolution.lang.MathLib;
import org.apache.commons.math3.util.FastMath;
import org.asoem.greyfish.cli.ScenarioParameter;
import org.asoem.greyfish.core.actions.*;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.conditions.GenericCondition;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.properties.ConstantProperty;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TileDirection;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.Product2;
import org.asoem.greyfish.utils.base.Tuple2;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import java.util.List;
import java.util.Map;

import static org.asoem.greyfish.core.individual.Callbacks.constant;
import static org.asoem.greyfish.utils.math.RandomUtils.*;


/**
 * User: christoph
 * Date: 01.06.12
 * Time: 13:40
 */
public class SimpleBranchingModel implements Provider<Scenario> {

    @Inject(optional = true)
    @ScenarioParameter("width")
    private String widthStr = "10";
    @Inject(optional = true)
    @ScenarioParameter("height")
    private String heightStr = "10";
    @Inject(optional = true)
    @ScenarioParameter("variance")
    private String variance = "0.01";

    @Override
    public Scenario get() {
        final int width = Integer.valueOf(this.widthStr);
        final Integer height = Integer.valueOf(heightStr);
        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(width, height)
                .addWallsHorizontal(0, width - 1, height / 2, TileDirection.NORTH)
                .build();

        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);

        final Agent prototype = createConsumerPrototype();
        for (int i = 0; i < 1000; ++i) {
            final ImmutableObject2D object2D = ImmutableObject2D.of(
                    nextDouble(width),
                    nextDouble(height),
                    nextDouble(MathLib.PI));
            scenarioBuilder.addAgent(new Avatar(prototype), object2D);
        }

        return scenarioBuilder.build();
    }

    private Agent createConsumerPrototype() {
        return ImmutableAgent.of(Population.named("SexualPopulation"))
                .addActions(
                        DeathAction.with()
                                .name("die")
                                .executesIf(GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                    private static final double SIGMA = 1.0;
                                    private static final double DISTANCE = 1.1; // 0.5 viability at 0.0

                                    @Override
                                    public Boolean apply(GenericCondition caller, Map<String, ?> arguments) {
                                        return caller.agent().getAge() == 1
                                                && RandomUtils.trueWithProbability(
                                                    viability(caller.agent().getProperty("habitat", GFProperty.class).getValue(),
                                                        caller.agent().getGene("consumer_classification", GeneComponent.class).getAllele()))
                                                || caller.agent().getAge() >= 500
                                                || caller.agent().getAction("reproduce", SexualReproductionAction.class).getCompletionCount() > 0;
                                    }

                                    private double viability(Object habitat, Object allele) {
                                        if("habitat_1".equals(habitat)) {
                                            return gaussian((Double)allele, DISTANCE);
                                        }
                                        else if ("habitat_2".equals(habitat)) {
                                            return gaussian((Double)allele, -DISTANCE);
                                        }
                                        else
                                            throw new AssertionError("Unknown habitat " + habitat);
                                    }

                                    private double gaussian(double value, double distance) {
                                        return FastMath.exp(-FastMath.pow(value + distance, 2)/2*FastMath.pow(SIGMA, 2));
                                    }
                                }))
                                .build(),
                        SexualReproductionAction.with()
                                .name("reproduce")
                                .clutchSize(new Callback<SexualReproductionAction, Integer>() {
                                    private final int maxOffspring = 10;

                                    @Override
                                    public Integer apply(final SexualReproductionAction caller, Map<String, ?> arguments) {
                                        int ret = 0;

                                        final int frequency = Iterables.frequency(Iterables.transform(caller.simulation().getAgents(), new Function<Agent, Object>() {
                                            @Override
                                            public Object apply(Agent input) {
                                                return input.getProperty("habitat", ConstantProperty.class).getValue();
                                            }
                                        }), caller.agent().getProperty("habitat", ConstantProperty.class).getValue());

                                        final int probability = 1 - frequency / 1000;

                                        for (int i = 0; i < maxOffspring; i++) {
                                            ret += fromBoolean(RandomUtils.trueWithProbability(probability));
                                        }
                                        return ret;
                                    }

                                    private int fromBoolean(boolean b) {
                                        return b ? 1 : 0;
                                    }
                                })
                                .spermSupplier(new Callback<SexualReproductionAction, List<? extends Chromosome>>() {
                                    @Override
                                    public List<? extends Chromosome> apply(SexualReproductionAction caller, Map<String, ?> arguments) {
                                        return caller.agent().getAction("receive", MatingReceiverAction.class).getReceivedSperm();
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() > 0;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition caller, Map<String, ?> arguments) {
                                                final SexualReproductionAction action = caller.agent().getAction("reproduce", SexualReproductionAction.class);
                                                return action.getCompletionCount() == 0 || caller.simulation().getStep() - action.lastCompletionStep() > 10;
                                            }
                                        })
                                ))
                                .build(),
                        MatingTransmitterAction.with()
                                .name("fertilize")
                                .ontology("mate")
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("MALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getAction("fertilize", MatingTransmitterAction.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(constant(1.0))
                                .matingProbability(Callbacks.constant(1.0))
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Map<String, ?> arguments) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
                                .build(),
                        GenericMovementAction.builder()
                                .name("move")
                                .stepSize(Callbacks.constant(1.0))
                                .turningAngle(new Callback<GenericMovementAction, Double>() {
                                    @Override
                                    public Double apply(GenericMovementAction caller, Map<String, ?> arguments) {
                                        final double rotation = caller.agent().getMotion().getRotation();
                                        if (caller.agent().didCollide())
                                            return rotation + MathLib.PI;
                                        return rotation + RandomUtils.rnorm(0, 0.08);
                                    }
                                })
                                .build())
                .addProperties(
                        ConstantProperty.<String>builder()
                                .name("habitat")
                                .callback(new Callback<ConstantProperty<String>, String>() {
                                    @Override
                                    public String apply(ConstantProperty<String> caller, Map<String, ?> arguments) {
                                        final boolean b = RandomUtils.nextBoolean();
                                        return b ? "habitat_1" : "habitat_2";
                                    }
                                })
                                .build()
                )
                .addGenes(
                        MarkovGeneComponent.builder()
                                .name("gender")
                                .put("MALE", "FEMALE", Callbacks.constant(0.5))
                                .put("FEMALE", "MALE", Callbacks.constant(0.5))
                                .initialState(new Callback<MarkovGeneComponent, String>() {
                                    @Override
                                    public String apply(MarkovGeneComponent caller, Map<String, ?> arguments) {
                                        return sample("MALE", "FEMALE");
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialAllele(Callbacks.constant(0.0))
                                .mutation(new Callback<DoubleGeneComponent, Double>() {

                                    private Double sd = Double.valueOf(variance);;

                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        return ((Double) arguments.get("original")) + rnorm(0, sd);
                                    }
                                })
                                .recombination(new Callback<DoubleGeneComponent, Product2<Double, Double>>() {
                                    @Override
                                    public Product2<Double, Double> apply(DoubleGeneComponent caller, Map<String, ?> arguments) {
                                        final double v = (((Double) arguments.get("first")) + ((Double) arguments.get("second"))) / 2;
                                        return Tuple2.of(v, v);
                                    }
                                })
                                .build()
                )
                .build();
    }
}
