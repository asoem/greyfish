package org.asoem.greyfish.core.interfaces;

import java.util.Map;

import javolution.lang.MathLib;

import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

public class Movement2DAcutator extends AbstractGFComponent implements GFInterface {
	
	public Movement2DAcutator() {
	}
		
	public Movement2DAcutator(Movement2DAcutator component,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(component, mapDict);
	}

	public void rotate(Simulation simulation, final float angle) {
		simulation.enqueAfterStepCommand(new Command() {		
			@Override
			public void execute() {
				componentOwner.rotate(angle);
			}
		});
	}

	public void translate(final Simulation simulation, double distance) {

		final float x_add = (float) (distance * Math.cos(componentOwner.getOrientation()));
		final float y_add = (float) (distance * Math.sin(componentOwner.getOrientation()));

		final float x_res = componentOwner.getAnchorPoint().getX() + x_add;
		final float y_res = componentOwner.getAnchorPoint().getY() + y_add;

		simulation.enqueAfterStepCommand(new Command() {
			@Override
			public void execute() {
				Location2DInterface newLocation2d = new Location2D(x_res, y_res);
				Location2DInterface location2d = simulation.getSpace().moveObject(componentOwner, newLocation2d);
				
				if ( ! newLocation2d.equals(location2d)) { // collision
					componentOwner.rotate((float)MathLib.PI);
				}
			}
		});
	}

	@Override
	protected Movement2DAcutator deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Movement2DAcutator(this, mapDict);
	}
}
