package org.asoem.greyfish.core.interfaces;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.Command;

public final class Movement2DAcutator extends AbstractGFComponent implements GFInterface {
	
	private Movement2DAcutator(Builder builder) {
        super(builder);
	}

    protected Movement2DAcutator(Movement2DAcutator acutator, CloneMap map) {
        super(acutator, map);
    }

    public static Movement2DAcutator newInstance() {
        return new Builder().build();
    }

    public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<Movement2DAcutator> {
        @Override
        protected Builder self() {
            return this;
        }
        public Movement2DAcutator build() { return new Movement2DAcutator(this); }
    }

    @Override
    protected AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new Movement2DAcutator(this, map);
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
                Location2D newLocation = Location2D.at(x_res, y_res);
				simulation.getSpace().moveObject(componentOwner, newLocation);
				
				if ( ! componentOwner.getAnchorPoint().equals(newLocation)) { // collision
					componentOwner.rotate(MathLib.PI);
				}
			}
		});
	}
}
