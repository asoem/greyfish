package org.asoem.greyfish.core.individual;

import javolution.lang.MathLib;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.utils.RandomUtils;
import org.simpleframework.xml.Element;

import java.awt.*;

public class Body extends MovingObject2D implements Initializeable {

    private final static float DEFAULT_SPEED = 0.05f;
    private final static float DEFAULT_RADIUS = 0.1f;
    private final static Color DEFAULT_COLOR = Color.BLACK;

    @Element(name="color")
    private Color color = DEFAULT_COLOR;

    private Body() {
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
        setSpeed(DEFAULT_SPEED);
    }

    private Body(Body body) {
        anchorPoint.set(body.getAnchorPoint());
        setOrientation(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
        color = body.color;
        setSpeed(DEFAULT_SPEED);
    }

    public float getRadius() {
        return DEFAULT_RADIUS;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void initialize(Simulation simulation) {
        this.color = DEFAULT_COLOR;
    }

    public static Body newInstance() {
        return new Body();
    }

    public static Body copyOf(Body body) {
        return new Body(body);
    }
}
