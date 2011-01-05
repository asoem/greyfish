package org.asoem.sico.core.individual;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;

import org.asoem.sico.utils.Rainbow;
import org.asoem.sico.utils.RandomUtils;
import org.simpleframework.xml.Element;


public class Population implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4989452649218269512L;

	@Element(name="name", required = false)
	private String name;

	@Element(name="color")
	private Color color;

	public Population(String name) {
		this.name = name;
		setColor(Rainbow.getInstance().createRainbow(256)[RandomUtils.nextInt(256)]);
	}

	public Population() {
		this("");
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) || ((Population) obj).getName().equals(getName());
	}
	
	public static Population[] fromIndiviuduals(Collection<Individual> individuals) {
		return fromIndiviuduals((Individual[]) individuals.toArray(new Individual[individuals.size()]));
	}

	public static Population[] fromIndiviuduals(Individual ...individuals) {
		final Population[] ret = new Population[individuals.length];
		for (int i = 0; i < individuals.length; i++) {
			ret[i] = individuals[i].getPopulation();
		}
		return ret;
	}
}
