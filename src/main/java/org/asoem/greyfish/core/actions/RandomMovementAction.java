package org.asoem.greyfish.core.actions;

import java.util.Map;

import org.asoem.greyfish.core.interfaces.Movement2DAcutator;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.RandomUtils;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Attribute;

@ClassGroup(tags = "action")
public class RandomMovementAction extends AbstractGFAction {

	@Attribute(required=false)
	private double speed;

	private Movement2DAcutator movementAcutator;
	
	public RandomMovementAction() {
	}

	public RandomMovementAction(String name) {
		super(name);
	}

	public RandomMovementAction(RandomMovementAction action,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(action, mapDict);
	}

	@Override
	protected void performAction(Simulation simulation) {
		// rotate
		if (RandomUtils.nextBoolean()) {
			float phi = RandomUtils.nextFloat(0f, 0.1f);
			movementAcutator.rotate(simulation, phi);
		}

		// translate
		movementAcutator.translate(simulation, componentOwner.getBody().getSpeed());
	}

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new RandomMovementAction(this, mapDict);
	}

	@Override
	public void initialize(Simulation simulation) {
		super.initialize(simulation);
		movementAcutator = componentOwner.getInterface(Movement2DAcutator.class);
	}
	
	@Override
	public void export(Exporter e) {
		super.export(e);
		e.addField(new ValueAdaptor<Double>("", Double.class, speed) {

			@Override
			protected void writeThrough(Double arg0) {
				speed = arg0;
			}
		});
	}
}
