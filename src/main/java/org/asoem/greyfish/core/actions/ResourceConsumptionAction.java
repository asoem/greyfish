package org.asoem.greyfish.core.actions;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.ACLPerformative;
import org.asoem.greyfish.core.acl.NotUnderstoodException;
import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.properties.DoubleProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;

import java.util.Map;

@ClassGroup(tags="actions")
public class ResourceConsumptionAction extends ContractNetInitiatiorAction {

    @Element(name = "property")
    private DoubleProperty consumerProperty = null;

    @Element(name="messageType", required=false)
    private String parameterMessageType = "";

    @Element(name="amountPerRequest", required=false)
    private double amountPerRequest = 0;

    @Element(name="sensorRange")
    private double sensorRange = 0;

    private Iterable<Individual> sensedMates;

    public ResourceConsumptionAction() {
    }


    protected ResourceConsumptionAction(ResourceConsumptionAction action,
                                        Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        super(action, mapDict);
        consumerProperty = deepClone(action.consumerProperty, mapDict);
        parameterMessageType = action.parameterMessageType;
        amountPerRequest = action.amountPerRequest;
        sensorRange = action.sensorRange;
    }


    @Override
    protected ACLMessage createCFP() {
        ACLMessage message = ACLMessage.newInstance();
        message.setPerformative(ACLPerformative.CFP);
        message.setOntology(getOntology());
        // Choose only one receiver. Adding all possible candidates as receivers will decrease the performance in high density populations!
        message.addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))));
        message.setReferenceContent(amountPerRequest);
        return message;
    }

    @Override
    protected ACLMessage handlePropose(ACLMessage message) throws NotUnderstoodException {
        assert(message != null);

        final Object messageContent = message.getReferenceContent();

        try {
            final double offer = (Double) messageContent;
            consumerProperty.add(offer);

            ACLMessage replyMessage = message.createReply();
            replyMessage.setPerformative(ACLPerformative.ACCEPT_PROPOSAL);
            return replyMessage;
        } catch (Exception e) {
            throw new NotUnderstoodException();
        }

    }

    @Override
    protected String getOntology() {
        return parameterMessageType;
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new ResourceConsumptionAction(this, mapDict);
    }

    @Override
    public boolean evaluate(Simulation simulation) {
        if ( super.evaluate(simulation) ) {
            sensedMates = Iterables.filter(simulation.getSpace().findNeighbours(componentOwner, sensorRange), Individual.class);
            sensedMates = Iterables.filter(sensedMates, Predicates.not(Predicates.equalTo(componentOwner)));
            return ! Iterables.isEmpty(sensedMates);
        }
        return false;
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        checkValidity();
    }

    private void checkValidity() {
        Preconditions.checkNotNull(consumerProperty);
        Preconditions.checkNotNull(parameterMessageType);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueAdaptor<String>("Ontology", String.class, parameterMessageType) {
            @Override protected void writeThrough(String arg0) {
                parameterMessageType = arg0;
            }
        });
        e.addField(new ValueAdaptor<Double>("Amount", Double.class, amountPerRequest) {
            @Override protected void writeThrough(Double arg0) {
                amountPerRequest = arg0;
            }
        });
        e.addField(new ValueSelectionAdaptor<DoubleProperty>("Destination", DoubleProperty.class, consumerProperty, componentOwner.getProperties(DoubleProperty.class)) {
            @Override protected void writeThrough(DoubleProperty arg0) {
                consumerProperty = arg0;
            }
        });
        e.addField(new ValueAdaptor<Double>("Sensor Range", Double.class, sensorRange) {

            @Override
            protected void writeThrough(Double arg0) {
                sensorRange = arg0;
            }
        });
    }
}
