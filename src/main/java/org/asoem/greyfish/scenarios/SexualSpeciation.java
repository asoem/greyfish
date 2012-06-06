package org.asoem.greyfish.scenarios;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Provider;
import javolution.lang.MathLib;
import org.asoem.greyfish.cli.ScenarioParameter;
import org.asoem.greyfish.core.actions.*;
import org.asoem.greyfish.core.conditions.AllCondition;
import org.asoem.greyfish.core.conditions.FunctionCondition;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.DoubleGeneComponent;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.MarkovGeneComponent;
import org.asoem.greyfish.core.individual.*;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.base.Product2;
import org.asoem.greyfish.utils.base.Tuple2;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static org.asoem.greyfish.core.individual.Callbacks.constant;
import static org.asoem.greyfish.utils.math.RandomUtils.*;


/**
 * User: christoph
 * Date: 01.06.12
 * Time: 13:40
 */
public class SexualSpeciation implements Provider<Scenario> {

    @Inject(optional=true) @ScenarioParameter("width") private String width = "10";
    @Inject(optional=true) @ScenarioParameter("height") private String height = "10";
    @Inject(optional=true) @ScenarioParameter("variance") private String variance = "0.01";

    @Override
    public Scenario get() {
        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(Integer.valueOf(width), Integer.valueOf(height)).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);

        final Agent prototype = createConsumerPrototype();
        for (int i = 0; i < 500; ++i) {
            final ImmutableObject2D object2D = ImmutableObject2D.of(
                    nextDouble(Integer.valueOf(width)),
                    nextDouble(Integer.valueOf(height)),
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
                                .executesIf(FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                    @Override
                                    public Boolean apply(FunctionCondition functionCondition) {
                                        return functionCondition.agent().getAge() >= 500;
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
                                            public Boolean apply(FunctionCondition functionCondition) {
                                                return RandomUtils.trueWithProbability(1 - functionCondition.agent().getSimulationContext().getSimulation().countAgents() / 2000);
                                            }
                                        })
                                ))
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
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("fertilize", MatingTransmitterAction.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
                                .build(),
                        MatingReceiverAction.with()
                                .name("receive")
                                .ontology("mate")
                                .interactionRadius(constant(1.0))
                                .matingProbability(new Callback<MatingReceiverAction, Double>() {
                                    @Override
                                    public Double apply(MatingReceiverAction caller, Map<String, ?> localVariables) {
                                        final Double classificationOfMate = (Double) ((Agent) localVariables.get("mate")).getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final Double myClassification = (Double) caller.agent().getGene("consumer_classification", GeneComponent.class).getAllele();
                                        final double maximalDifference = 0.03;

                                        final boolean b = abs(classificationOfMate - myClassification) < maximalDifference;
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
                                                return condition.agent().getAge() >= 200;
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() == 0;
                                            }
                                        })
                                ))
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
                                        return sample("MALE", "FEMALE");
                                    }
                                })
                                .build(),
                        DoubleGeneComponent.builder()
                                .name("consumer_classification")
                                .initialValue(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return 0.1;
                                    }
                                })
                                .mutation(new Callback<DoubleGeneComponent, Double>() {
                                    @Override
                                    public Double apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        return ((Double) localVariables.get("original")) + rnorm(0, Double.valueOf(variance));
                                    }
                                })
                                .recombination(new Callback<DoubleGeneComponent, Product2<Double, Double>>() {
                                    @Override
                                    public Product2<Double, Double> apply(DoubleGeneComponent caller, Map<String, ?> localVariables) {
                                        final double v = (((Double) localVariables.get("first")) + ((Double) localVariables.get("second"))) / 2;
                                        return Tuple2.of(v, v);
                                    }
                                })
                                .build()
                )
                .build();
    }
}
