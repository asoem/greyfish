package org.asoem.greyfish.core.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.individual.ImmutableAgent;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.EvaluatedGenomeStorage;
import org.asoem.greyfish.core.scenario.BasicScenario;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.MutableObject2D;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.runner.RunWith;

import java.awt.*;

import static org.asoem.greyfish.core.space.MutableObject2D.at;

@RunWith(JDaveRunner.class)
public class MatingInteractionSpec extends Specification<ContractNetInitiatorAction> {
    public class NormalInteraction {

        final EvaluatedGenomeStorage genomeStorage = EvaluatedGenomeStorage.with().build();
        final MatingReceiverAction receiverAction = MatingReceiverAction.with().fromMatesOfType("mate").closerThan(1.0).storesSpermIn(genomeStorage).build();
        final Agent female = ImmutableAgent.of(null).population(Population.newPopulation("Female", Color.black)).addProperties(genomeStorage).addActions(receiverAction).build();

        final MatingTransmitterAction transmitterAction = MatingTransmitterAction.with().offersSpermToMatesOfType("mate").build();
        final Agent male = ImmutableAgent.of(null).population(Population.newPopulation("Male", Color.black)).addActions(transmitterAction).build();

        final Scenario scenario = BasicScenario.builder("TestScenario", TiledSpace.ofSize(1, 1))
                .addAgent(female, MutableObject2D.locatedAt(0, 0))
                .addAgent(male, MutableObject2D.locatedAt(0, 0))
                .build();

        final ParallelizedSimulation simulation = ParallelizedSimulation.newSimulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 3;
            while (stepRequired-- != 0) {
                simulation.step();
            }

            Agent consumerClone = Iterables.find(simulation.getAgents(), new Predicate<Agent>() {
                @Override
                public boolean apply(Agent agent) {
                    return agent.getPopulation().equals(female.getPopulation());
                }
            }, null);
            assert consumerClone != null;

            specify(Iterables.get(Iterables.filter(consumerClone.getProperties(), EvaluatedGenomeStorage.class), 0).get().size(), should.equal(1));
        }

    }
}
