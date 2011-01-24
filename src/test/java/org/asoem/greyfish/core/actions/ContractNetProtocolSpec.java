package org.asoem.greyfish.core.actions;

import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(JDaveRunner.class)
public class ContractNetProtocolSpec extends Specification<ContractNetInitiatiorAction> {
    public class NormalInteraction {
         private class SimpleContractNetInitiatiorAction extends ContractNetInitiatiorAction {

             private SimpleContractNetInitiatiorAction() {
                 super(new AbstractBuilder() {
                     @Override
                     protected org.asoem.greyfish.lang.AbstractBuilder self() {
                         return this;
                     }
                 });
             }

             @Override
             protected ACLMessage.Builder createCFP() {
                 return ACLMessage.with();
             }

             @Override
             protected ACLMessage.Builder handlePropose(ACLMessage message) throws NotUnderstoodException {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }

             @Override
             protected String getOntology() {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }

             @Override
             protected AbstractDeepCloneable deepCloneHelper(Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
                 return null;  //To change body of implemented methods use File | Settings | File Templates.
             }
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
