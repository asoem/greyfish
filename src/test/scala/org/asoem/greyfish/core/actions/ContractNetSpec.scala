package org.asoem.greyfish.core.actions

import org.scalatest.FlatSpec
import org.asoem.greyfish.core.individual.Individual
import org.asoem.greyfish.core.actions._
import org.asoem.greyfish.core.simulation.Simulation
import org.asoem.greyfish.core.scenario.Scenario
import org.asoem.greyfish.utils.AbstractDeepCloneable
import java.util.Map
import org.asoem.greyfish.core.acl.{ACLPerformative, ACLMessage}
import java.lang.String
import org.asoem.greyfish.core.space.{Location2D, Placeholder, TiledSpace}

/**
 * Created by IntelliJ IDEA.
 * User: christoph
 * Date: 17.01.11
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */

class ContractNetSpec extends FlatSpec {

  "Two communication partner" should "correctly do a ContractNet protocol" in {

    trait SimpleContractNetInitiatiorAction extends ContractNetInitiatiorAction {

      protected[actions] def getOntology = "test"

      protected[actions] def handlePropose(message: ACLMessage) : ACLMessage = {
        val ret = message.createReply
        ret.setPerformative(ACLPerformative.ACCEPT_PROPOSAL)
        ret
      }

      protected[actions] def createCFP : ACLMessage = {
        val ret = ACLMessage.newInstance
        ret.setPerformative(ACLPerformative.CFP)
        ret
      }

      protected[SimpleContractNetInitiatiorAction] def deepCloneHelper(mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable]) : SimpleContractNetInitiatiorAction = {
        return SimpleContractNetInitiatiorAction(this, mapDict)
      }
    }

    object SimpleContractNetInitiatiorAction {
      def apply() = new SimpleContractNetInitiatiorAction
      def apply(clone : SimpleContractNetInitiatiorAction, mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable])
        = new ContractNetInitiatiorAction(clone, mapDict) with SimpleContractNetInitiatiorAction
    }

    trait SimpleContractNetResponderAction extends ContractNetResponderAction {
      protected[SimpleContractNetResponderAction] def deepCloneHelper(mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable]): SimpleContractNetResponderAction
        = SimpleContractNetResponderAction(this, mapDict)

      protected[actions] def handleCFP(message: ACLMessage): ACLMessage = {
        val ret = message.createReply
        ret.setPerformative(ACLPerformative.PROPOSE)
        ret
      }

      protected[actions] def handleAccept(message: ACLMessage): ACLMessage = {
        val ret = message.createReply
        ret.setPerformative(ACLPerformative.INFORM)
        ret
      }

      protected[actions] def getOntology: String = "test"
    }

    object SimpleContractNetResponderAction {
      def apply() = new ContractNetResponderAction with SimpleContractNetResponderAction
      def apply(clone : SimpleContractNetResponderAction, mapDict: Map[AbstractDeepCloneable, AbstractDeepCloneable]) = new ContractNetResponderAction(clone, mapDict) with SimpleContractNetResponderAction
    }

    val initiator = new Individual();
    val initAction = SimpleContractNetInitiatiorAction();
    initiator.addAction(initAction);

    val participant = new Individual();
    val responderAction = SimpleContractNetResponderAction();
    participant.addAction(responderAction);

    val space = new TiledSpace(1, 1);
    val scenario = new Scenario(space);
    scenario.addPlaceholder(new Placeholder(new Location2D(0,0), initiator));
    scenario.addPlaceholder(new Placeholder(new Location2D(0,0), participant));

    val sim = new Simulation(scenario);

    assert(false, "Test not completely implemented yet")
  }
}