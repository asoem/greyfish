package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.utils.space.Coordinates2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Element;


/**
 * An unmodifiable view of an agent at a specified location
 *
 * TODO: it is not enforced yet that the agent is unmodifiable!
 */
public class Placeholder extends AgentDecorator implements Object2D {

    @Element(name = "placeholderObject")
    private final Object2D placeholderObject;

    public Placeholder(@Element(name = "delegate") Agent prototype,
                       @Element(name = "placeholderObject") Object2D placeholderObject) {
        super(prototype);
        this.placeholderObject = placeholderObject; // TODO: store an immutable copy of placeholderObject
    }

    public static Placeholder newInstance(Agent prototype, Object2D location2D) {
        return new Placeholder(prototype, location2D);
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Coordinates2D getCoordinates() {
        return placeholderObject.getCoordinates();
    }

    @Override
    public double getOrientation() {
        return placeholderObject.getOrientation();
    }

    @Override
    public void setRotation(double alpha) {
        throw new UnsupportedOperationException();
    }
}
