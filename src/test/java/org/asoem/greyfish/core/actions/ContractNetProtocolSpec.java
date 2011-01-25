package org.asoem.greyfish.core.actions;

import com.google.common.collect.Iterables;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.individual.Population;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.properties.ResourceProperty;
import org.asoem.greyfish.core.scenario.Scenario;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Placeholder;
import org.asoem.greyfish.core.space.TiledSpace;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
public class ContractNetProtocolSpec extends Specification<ContractNetInitiatiorAction> {
    public class NormalInteraction {

        DoubleProperty energyStorage = DoubleProperty.with().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        ResourceConsumptionAction consumptionAction =
                ResourceConsumptionAction.with().viaMessagesOfType("test").requesting(1).storesEnergyIn(energyStorage).build();
        Individual i1 = Individual.with().population(new Population("TestPop1")).addProperties(energyStorage).addActions(consumptionAction).build();

        ResourceProperty resourceProperty = new ResourceProperty.Builder().lowerBound(0.0).upperBound(1.0).initialValue(0.0).build();
        ResourceProvisionAction provisionAction = ResourceProvisionAction.with().parameterMessageType("test").resourceProperty(resourceProperty).build();
        Individual i2 = Individual.with().population(new Population("TestPop2")).addProperties(resourceProperty).addActions(provisionAction).build();

        TiledSpace space = new TiledSpace(1,1);
        Scenario scenario = Scenario.with().space(space)
                .addPlaceholder(new Placeholder(new Location2D(0,0), i1))
                .addPlaceholder(new Placeholder(new Location2D(0,0), i2))
                .build();

        Simulation s = new Simulation(scenario);

        public void shouldTransferTheCorrectAmount() {
            int stepRequired = 3;
            while (stepRequired-- != 0) {
                s.step();
            }

            specify(Iterables.get(s.getIndividuals().get(1).getProperties(DoubleProperty.class), 0).getValue(), should.equal(1.0));
        }

    }

    /*
    class ContractNetSpec extends FlatSpec {

  "Two communication partner" should "correctly do a ContractNet protocol" in {

    class SimpleContractNetInitiatiorAction(builder : AbstractGFAction.AbstractBuilder[_]) extends ContractNetInitiatiorAction(builder) {

      protected[actions] def getOntology = "test"

      protected[actions] def handlePropose(message: ACLMessage) : ACLMessage = {
        val ret = message.replyFrom
        ret.setPerformative(ACLPerformative.ACCEPT_PROPOSAL)
        ret
      }

      protected[actions] def createCFP : ACLMessage = {
        val ret = ACLMessage.newInstance
        ret.setPerformative(ACLPerformative.CFP)
        ret
      }

      protected override def deepClone(mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable]): DeepClonable =
        new SimpleContractNetInitiatiorAction.Builder().fromClone(this, mapDict).build
    }

    object SimpleContractNetInitiatiorAction {
       final class Builder extends AbstractBuilder[Builder] {
        def self() : Builder = this
      }

      protected class AbstractBuilder[T >: this.type] extends AbstractGFAction.AbstractBuilder[T] {
        def fromClone(executionCountOf : SimpleContractNetInitiatiorAction, map : Map[AbstractDeepCloneable, AbstractDeepCloneable]) : T = {
          super.fromClone(executionCountOf, map)
        }
        def build() : SimpleContractNetInitiatiorAction = { new SimpleContractNetInitiatiorAction(this) }
      }
    }

    class SimpleContractNetResponderAction(builder : AbstractGFAction.AbstractBuilder[_]) extends ContractNetResponderAction(builder) {

      protected[actions] override def handleCFP(message: ACLMessage): ACLMessage = {
        val ret = message.replyFrom
        ret.setPerformative(ACLPerformative.PROPOSE)
        ret
      }

      protected[actions] override def handleAccept(message: ACLMessage): ACLMessage = {
        val ret = message.replyFrom
        ret.setPerformative(ACLPerformative.INFORM)
        ret
      }

      protected[actions] override def getOntology: String = "test"

      protected override def deepClone(mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable]): DeepClonable =
        new SimpleContractNetResponderAction.Builder().fromClone(this, mapDict).build
    }

    object SimpleContractNetResponderAction {
       final class Builder extends AbstractBuilder[Builder] {
        def self() : Builder = this
      }

      protected class AbstractBuilder[T >: this.type] extends AbstractGFAction.AbstractBuilder[T] {
        def fromClone(executionCountOf : SimpleContractNetResponderAction, map : Map[AbstractDeepCloneable, AbstractDeepCloneable]) : T = {
          super.fromClone(executionCountOf, map)
        }

        def build() : SimpleContractNetResponderAction = { new SimpleContractNetResponderAction(this) }
      }
    }

    val initiator = new Individual();
    val initAction = new SimpleContractNetInitiatiorAction.Builder().build;
    initiator.addAction(initAction);

    val participant = new Individual();
    val responderAction = new SimpleContractNetResponderAction.Builder().build;
    participant.addAction(responderAction);

    val space = new TiledSpace(1, 1);
    val scenario = new Scenario(space);
    scenario.addPlaceholder(new Placeholder(new Location2D(0,0), initiator));
    scenario.addPlaceholder(new Placeholder(new Location2D(0,0), participant));

    val sim = new Simulation(scenario);

    assert(false, "Test not completely implemented yet")
  }
}
     */
}
