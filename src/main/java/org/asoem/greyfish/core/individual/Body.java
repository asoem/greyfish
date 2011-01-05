package org.asoem.sico.core.individual;

import java.awt.Color;

import javolution.lang.MathLib;

import org.asoem.sico.core.simulation.Initializeable;
import org.asoem.sico.core.simulation.Simulation;
import org.asoem.sico.core.space.Location2D;
import org.asoem.sico.core.space.MovingObject2D;
import org.asoem.sico.utils.RandomUtils;
import org.simpleframework.xml.Element;

public class Body extends MovingObject2D implements Initializeable {

	public Body(Individual individual) {
		this.individual = individual;
		rotate(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
		setSpeed(0.1f);
	}

	public Body(Individual individual, Location2D location2d) {
		this.individual = individual;
		anchorPoint.set(location2d);
		rotate(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
		setSpeed(0.1f);
	}

	public Body(Individual individual, Body body) {
		this.individual = individual;
		anchorPoint.set(body.getAnchorPoint());
		rotate(RandomUtils.nextFloat(0f, (float) MathLib.TWO_PI));
		color = body.color;
		setSpeed(0.1f);
	}

	// size of the body
	private final static float radius = 0.1f;
	
	private static final Color DEFAULT_COLOR = Color.BLACK;
	
	@Element(name="color")
	private Color color = DEFAULT_COLOR;
	
	private final Individual individual;

	public float getRadius() {
		return radius;
	}
	
	public Individual getIndividual() {
		return individual;
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
}
