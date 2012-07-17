package org.asoem.greyfish.scenarios;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javolution.lang.MathLib;
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
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TileDirection;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;
import org.asoem.greyfish.utils.space.MotionObject2D;

import java.util.List;

import static java.lang.Math.abs;
import static org.asoem.greyfish.utils.base.Callbacks.constant;
import static org.asoem.greyfish.utils.math.RandomUtils.*;


/**
 * User: christoph
 * Date: 01.06.12
 * Time: 13:40
 */
public class SexualSpeciation implements Provider<Scenario> {

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
                        Suicide.with()
                                .name("die")
                                .executesIf(GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                    @Override
                                    public Boolean apply(GenericCondition genericCondition, Arguments arguments) {
                                        return genericCondition.agent().getAge() >= 500 ||
                                                genericCondition.agent().getAction("reproduce", SexualReproduction.class).getCompletionCount() > 0;
                                    }
                                }))
                                .build(),
                        SexualReproduction.with()
                                .name("reproduce")
                                .clutchSize(new Callback<SexualReproduction, Integer>() {
                                    private final int maxOffspring = 10;

                                    @Override
                                    public Integer apply(final SexualReproduction caller, Arguments arguments) {
                                        int ret = 0;

                                        final int frequency = Iterables.frequency(Iterables.transform(caller.simulation().getAgents(), new Function<Agent, Object>() {
                                            @Override
                                            public Object apply(Agent input) {
                                                return input.getProperty("niche", ConstantProperty.class).getValue();
                                            }
                                        }), caller.agent().getProperty("niche", ConstantProperty.class).getValue());

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
                                .spermSupplier(new Callback<SexualReproduction, List<? extends Chromosome>>() {
                                    @Override
                                    public List<? extends Chromosome> apply(SexualReproduction caller, Arguments arguments) {
                                        return caller.agent().getAction("receive", FemaleLikeMating.class).getReceivedSperm();
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getAction("receive", FemaleLikeMating.class).getMatingCount() > 0;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition caller, Arguments arguments) {
                                                final SexualReproduction action = caller.agent().getAction("reproduce", SexualReproduction.class);
                                                return action.getCompletionCount() == 0 || caller.simulation().getStep() - action.lastCompletionStep() > 10;
                                            }
                                        })
                                ))
                                .build(),
                        MaleLikeMating.with()
                                .name("fertilize")
                                .ontology("mate")
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("MALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getAction("fertilize", MaleLikeMating.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
                                .build(),
                        FemaleLikeMating.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(constant(1.0))
                                .matingProbability(new Callback<FemaleLikeMating, Double>() {
                                    @Override
                                    public Double apply(FemaleLikeMating caller, Arguments arguments) {
                                        final Double classificationOfMate = (Double) ((Agent) arguments.get("mate")).getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final Double myClassification = (Double) caller.agent().getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final double maximalDifference = 0.03;

                                        final boolean b = abs(classificationOfMate - myClassification) < maximalDifference;
                                        return (b) ? 1.0 : 0.0;
                                    }
                                })
                                .executesIf(AllCondition.evaluates(
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getAllele().equals("FEMALE");
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        GenericCondition.evaluate(new Callback<GenericCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(GenericCondition condition, Arguments arguments) {
                                                return condition.agent().getAction("receive", FemaleLikeMating.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
                                .build(),
                        GenericMovement.builder()
                                .name("move")
                                .stepSize(Callbacks.constant(1.0))
                                .turningAngle(new Callback<GenericMovement, Double>() {
                                    @Override
                                    public Double apply(GenericMovement caller, Arguments arguments) {
                                        final double rotation = caller.agent().getMotion().getRotation();
                                        if (caller.agent().didCollide())
                                            return rotation + MathLib.PI;
                                        return rotation + RandomUtils.rnorm(0, 0.08);
                                    }
                                })
                                .build())
                .addProperties(
                        ConstantProperty.<String>builder()
                                .name("niche")
                                .callback(new Callback<ConstantProperty<String>, String>() {
                                    @Override
                                    public String apply(ConstantProperty<String> caller, Arguments arguments) {
                                        final MotionObject2D projection = caller.agent().getProjection();
                                        assert projection != null;
                                        return (projection.getAnchorPoint().getY() < (Double.valueOf(heightStr) / 2)) ? "upperHalf" : "lowerHalf";
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
                                    public String apply(MarkovGeneComponent caller, Arguments arguments) {
                                        return sample("MALE", "FEMALE");
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialAllele(Callbacks.constant(0.1))
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Arguments arguments) {
                                        return ((Double) arguments.get("original")) + rnorm(0, Double.valueOf(variance));
                                    }
                                })
                                .recombination(new Callback<DoubleGeneComponent, Product2<Double, Double>>() {
                                    @Override
                                    public Product2<Double, Double> apply(DoubleGeneComponent caller, Arguments arguments) {
                                        final double v = (((Double) arguments.get("first")) + ((Double) arguments.get("second"))) / 2;
                                        return Tuple2.of(v, v);
                                    }
                                })
                                .build()
                )
                .build();
    }
}
