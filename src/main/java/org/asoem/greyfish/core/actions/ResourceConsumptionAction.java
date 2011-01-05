package org.asoem.sico.core.actions;

import java.util.Map;

import org.asoem.sico.core.acl.ACLMessage;
import org.asoem.sico.core.acl.ACLPerformative;
import org.asoem.sico.core.individual.Individual;
import org.asoem.sico.core.properties.DoubleProperty;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;
import org.asoem.sico.utils.Exporter;
import org.asoem.sico.utils.RandomUtils;
import org.asoem.sico.utils.ValueAdaptor;
import org.asoem.sico.utils.ValueSelectionAdaptor;
import org.simpleframework.xml.Element;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

@ClassGroup(tags="action")
public class ResourceConsumptionAction extends ContractNetInitiatiorAction {

	@Element(name = "property")
	private DoubleProperty consumerProperty;

	@Element(name="messageType", required=false)
	private String parameterMessageType;

	@Element(name="amountPerRequest", required=false)
	private Double amountPerRequest;
	
	@Element(name="sensorRange")
	private double sensorRange;
	
	private Iterable<Individual> sensedMates;
	
	public ResourceConsumptionAction() {
	}
	
	
	protected ResourceConsumptionAction(ResourceConsumptionAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
		consumerProperty = deepClone(action.consumerProperty, mapDict);
		parameterMessageType = action.parameterMessageType;
		amountPerRequest = action.amountPerRequest;
	}


	@Override
	protected ACLMessage createCFP() {
		ACLMessage message = ACLMessage.newInstance();
		message.setPerformative(ACLPerformative.CFP);

		/*
		 * 
		 * Choose only one. Adding all possible candidates as receivers will decrease the performance in high density populations!
		 */
		message.addReceiver(Iterables.get(sensedMates, RandomUtils.nextInt(Iterables.size(sensedMates))));
		message.setOntology(parameterMessageType);
		message.setContent(String.valueOf(amountPerRequest));

		return message;
	}

	@Override
	protected ACLMessage handlePropose(ACLMessage message) {
		Double offer = Double.valueOf(message.getContent());
		consumerProperty.add(offer);
		
		ACLMessage replyMessage = message.createReply();
		replyMessage.setPerformative(ACLPerformative.ACCEPT_PROPOSAL);
		return replyMessage;
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
