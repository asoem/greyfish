package org.asoem.greyfish.core.interfaces;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.lang.Command;
import org.asoem.greyfish.utils.AbstractDeepCloneable;

import java.util.Map;

public final class Movement2DAcutator extends AbstractGFComponent implements GFInterface {
	
	private Movement2DAcutator(Builder builder) {
        super(builder);
	}

    public static Movement2DAcutator newInstance() {
        return new Builder().build();
    }

    public static class Builder extends AbstractBuilder<Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        protected Builder fromClone(Movement2DAcutator component, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            return super.fromClone(component, mapDict);
        }

        public Movement2DAcutator build() { return new Movement2DAcutator(this); }
    }

	@Override
	protected AbstractDeepCloneable deepCloneHelper(
			Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
		return new Builder().fromClone(this, mapDict).build();
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
}
