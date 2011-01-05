package org.asoem.sico.core.properties;

import java.util.Map;

import org.asoem.sico.core.share.Consumer;
import org.asoem.sico.core.share.ConsumerGroup;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.lang.ClassGroup;
import org.asoem.sico.utils.AbstractDeepCloneable;

@ClassGroup(tags="property")
public class ResourceProperty extends DoubleProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4267082118277134951L;

	private ConsumerGroup<DoubleProperty> consumerGroup;

	public ResourceProperty() {
		init(10, 0);
	}

	protected ResourceProperty(ResourceProperty property,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(property, mapDict);
		init(property.getUpperBound(), property.getInitialValue());
	}

	protected void init(double max, double initial) {
		setUpperBound(max);
		setInitialValue(initial);
	}

	public ConsumerGroup<? extends DoubleProperty> getConsumerGroup() {
		return consumerGroup;
	}

	public void addConsumer(Consumer<DoubleProperty> consumer) {
		consumerGroup.addConsumer(consumer);
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		consumerGroup = new ConsumerGroup<DoubleProperty>(getName());
	}
	
	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new ResourceProperty(this, mapDict);
	}
}
