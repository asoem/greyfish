package org.asoem.greyfish.core.interfaces;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

public class Movement2DAcutator extends AbstractGFComponent implements GFInterface {
	
	public Movement2DAcutator() {
	}
		
	public Movement2DAcutator(Movement2DAcutator component,
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		super(component, mapDict);
	}

	public void rotate(Simulation simulation, final double angle) {
		simulation.enqueAfterStepCommand(new Command() {		
			@Override
			public void execute() {
				componentOwner.rotate(angle);
			}
		});
	}

	public void translate(final Simulation simulation, double distance) {

		final double x_add = distance * Math.cos(componentOwner.getOrientation());
		final double y_add = distance * Math.sin(componentOwner.getOrientation());

		final double x_res = componentOwner.getAnchorPoint().getX() + x_add;
		final double y_res = componentOwner.getAnchorPoint().getY() + y_add;

		simulation.enqueAfterStepCommand(new Command() {
			@Override
			public void execute() {
				Location2DInterface newLocation2d = new Location2D(x_res, y_res);
				Location2DInterface location2d = simulation.getSpace().moveObject(componentOwner, newLocation2d);
				
				if ( ! newLocation2d.equals(location2d)) { // collision
					componentOwner.rotate(MathLib.PI);
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
