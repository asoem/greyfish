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
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.space.TiledSpace;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableObject2D;

import javax.annotation.Nullable;
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

    @Override
    public Scenario get() {
        final TiledSpace<Agent> tiledSpace = TiledSpace.<Agent>builder(10, 10).build();
        final BasicScenario.Builder scenarioBuilder = BasicScenario.builder("SimpleSexualPopulation", tiledSpace);

        final Agent prototype = createConsumerPrototype();
        for (int i = 0; i<500; ++i) {
            scenarioBuilder.addAgent(new Avatar(prototype), ImmutableObject2D.of(nextDouble(10), nextDouble(10), nextDouble(MathLib.PI)));
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
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getValue().equals("FEMALE");
                                            }
                                        }),
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(FunctionCondition condition) {
                                                return condition.agent().getAction("receive", MatingReceiverAction.class).getMatingCount() > 0;
                                            }
                                        }) ,
                                        FunctionCondition.evaluate(new Function<FunctionCondition, Boolean>() {
                                            @Override
                                            public Boolean apply(@Nullable FunctionCondition functionCondition) {
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
                                                return condition.agent().getGene("gender", MarkovGeneComponent.class).getValue().equals("MALE");
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
                                        final Double classificationOfMate = (Double) ((Agent) localVariables.get("mate")).getGene("consumer_classification", GeneComponent.class).getValue();
                                        final Double myClassification = (Double) caller.agent().getGene("consumer_classification", GeneComponent.class).getValue();
                                        final double maximalDifference = 0.03;

                                        final boolean b = abs(classificationOfMate - myClassification) < maximalDifference;
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
                                        return trueWithProbability(0.0001) ? sample(-0.01, 0.01) : 0.0;
                                    }
                                })
                                .build()
                )
                .build();
    }
}
